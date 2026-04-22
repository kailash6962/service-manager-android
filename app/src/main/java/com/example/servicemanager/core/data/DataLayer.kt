package com.example.servicemanager.core.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.withTransaction
import com.example.servicemanager.core.domain.AddSparePartRequest
import com.example.servicemanager.core.domain.AddServiceOrderRequest
import com.example.servicemanager.core.domain.Customer
import com.example.servicemanager.core.domain.DiagnosticAnswer
import com.example.servicemanager.core.domain.DiagnosticSession
import com.example.servicemanager.core.domain.DiagnosticsRepository
import com.example.servicemanager.core.domain.Issue
import com.example.servicemanager.core.domain.LifecycleBucket
import com.example.servicemanager.core.domain.NotesRepository
import com.example.servicemanager.core.domain.PaymentSummary
import com.example.servicemanager.core.domain.PriorityLevel
import com.example.servicemanager.core.domain.RunDiagnosticsRequest
import com.example.servicemanager.core.domain.ServiceBuckets
import com.example.servicemanager.core.domain.ServiceNote
import com.example.servicemanager.core.domain.ServiceOrder
import com.example.servicemanager.core.domain.ServiceOrderRepository
import com.example.servicemanager.core.domain.ServiceStatus
import com.example.servicemanager.core.domain.ServiceSummary
import com.example.servicemanager.core.domain.SparePart
import com.example.servicemanager.core.domain.SparePartRepository
import com.example.servicemanager.core.domain.StatusTransition
import com.example.servicemanager.core.domain.StatusWorkflowRepository
import com.example.servicemanager.core.domain.SyncMetadata
import com.example.servicemanager.core.domain.SyncState
import com.example.servicemanager.core.domain.TechnicianAssignment
import com.example.servicemanager.core.domain.TimelineState
import com.example.servicemanager.core.domain.UpdateStatusRequest
import com.example.servicemanager.core.domain.WorkflowTimelineEntry
import com.example.servicemanager.core.domain.toBucket
import com.example.servicemanager.widgets.PendingServiceWidgetProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("sentinel_prefs")

enum class ServiceSortMode {
    UPDATED_DESC,
    UPDATED_ASC,
    PRIORITY_DESC,
}

data class CompanyProfile(
    val companyName: String = "Sentinel HUB",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val taxId: String = "",
    val otherDetails: String = "",
)

class AppTypeConverters {
    @TypeConverter
    fun fromSyncState(value: SyncState): String = value.name

    @TypeConverter
    fun toSyncState(value: String): SyncState = SyncState.valueOf(value)

    @TypeConverter
    fun fromServiceStatus(value: ServiceStatus): String = value.name

    @TypeConverter
    fun toServiceStatus(value: String): ServiceStatus = ServiceStatus.valueOf(value)

    @TypeConverter
    fun fromPriority(value: PriorityLevel): String = value.name

    @TypeConverter
    fun toPriority(value: String): PriorityLevel = PriorityLevel.valueOf(value)

    @TypeConverter
    fun fromDiagnosticAnswer(value: DiagnosticAnswer): String = value.name

    @TypeConverter
    fun toDiagnosticAnswer(value: String): DiagnosticAnswer = DiagnosticAnswer.valueOf(value)

    @TypeConverter
    fun fromTimelineState(value: TimelineState): String = value.name

    @TypeConverter
    fun toTimelineState(value: String): TimelineState = TimelineState.valueOf(value)

    @TypeConverter
    fun fromInvoiceStatus(value: com.example.servicemanager.core.domain.InvoiceStatus): String = value.name

    @TypeConverter
    fun toInvoiceStatus(value: String): com.example.servicemanager.core.domain.InvoiceStatus = com.example.servicemanager.core.domain.InvoiceStatus.valueOf(value)
}

@Entity(tableName = "status_configs")
data class StatusConfigEntity(
    @PrimaryKey val status: ServiceStatus,
    val estimatedMinutes: Int,
    val fixedTimeOfDayMinutes: Int? = null,
    val showQcWarningForIncomplete: Boolean = false,
    val isActive: Boolean = true,
)

@Entity(tableName = "brands")
data class BrandEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val name: String,
    val logoUrl: String? = null,
)

@Entity(tableName = "device_types")
data class DeviceTypeEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val name: String,
    val iconName: String? = null,
)

@Entity(tableName = "qc_checklist")
data class QCChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val title: String,
    val description: String? = null,
)

@Entity(tableName = "service_orders")
data class ServiceOrderEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val uuid: String,
    val serviceCode: String,
    val status: ServiceStatus,
    val priority: PriorityLevel,
    val intent: String,
    val reportedProblem: String,
    val updatedAt: Long,
    val syncState: SyncState,
    val deletedAt: Long? = null,
)

@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serviceId: Long,
    val name: String,
    val phone: String,
    val type: String,
)

@Entity(tableName = "customer_profiles")
data class CustomerProfileEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val company: String = "",
    val address: String = "",
    val type: String = "Individual",
)

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serviceId: Long,
    val type: String,
    val brand: String,
    val model: String,
    val serialNumber: String,
)

@Entity(
    tableName = "issues",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class IssueEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serviceId: Long,
    val title: String,
    val requirement: String,
)

@Entity(
    tableName = "spare_parts",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SparePartEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val uuid: String,
    val serviceId: Long,
    val name: String,
    val storageBoxNumber: String,
    val assignedStation: String,
    val inventoryLevel: String,
    val updatedAt: Long,
    val syncState: SyncState,
    val deletedAt: Long? = null,
)

@Entity(
    tableName = "diagnostic_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class DiagnosticSessionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val uuid: String,
    val serviceId: Long,
    val primaryPowerRailConnection: DiagnosticAnswer,
    val dataLinkHandshake: DiagnosticAnswer,
    val internalSensorCalibration: DiagnosticAnswer,
    val qcTotal: Int,
    val qcPassed: Int,
    val qcFailed: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState,
    val deletedAt: Long? = null,
)

@Entity(
    tableName = "status_transitions",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StatusTransitionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serviceId: Long,
    val fromStatus: ServiceStatus,
    val toStatus: ServiceStatus,
    val note: String,
    val createdAt: Long,
)

@Entity(
    tableName = "workflow_timeline_entries",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class WorkflowTimelineEntryEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serviceId: Long,
    val title: String,
    val subtitle: String,
    val state: TimelineState,
)

@Entity(
    tableName = "service_notes",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ServiceNoteEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val uuid: String,
    val serviceId: Long,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState,
    val deletedAt: Long? = null,
)

@Entity(
    tableName = "technician_assignments",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TechnicianAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serviceId: Long,
    val technicianName: String,
    val role: String,
)

@Entity(
    tableName = "payment_summaries",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PaymentSummaryEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serviceId: Long,
    val initialEstimate: Double,
    val advancePaid: Double,
)

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serviceId: Long,
    val amount: Double,
    val status: com.example.servicemanager.core.domain.InvoiceStatus,
    val createdAt: Long,
)

data class ServiceSummaryProjection(
    val localId: Long,
    val serviceCode: String,
    val status: ServiceStatus,
    val priority: PriorityLevel,
    val updatedAt: Long,
    val createdAt: Long,
    val customerName: String,
    val type: String,
    val brand: String,
    val model: String,
)

data class CustomerSearchProjection(
    val name: String,
    val phone: String,
    val type: String,
)

data class CustomerDirectoryProjection(
    val name: String,
    val phone: String,
    val type: String,
)

data class ServiceAggregate(
    @Embedded val service: ServiceOrderEntity,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val customer: List<CustomerEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val device: List<DeviceEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val issues: List<IssueEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val spareParts: List<SparePartEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val diagnostics: List<DiagnosticSessionEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val transitions: List<StatusTransitionEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val timeline: List<WorkflowTimelineEntryEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val notes: List<ServiceNoteEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val assignments: List<TechnicianAssignmentEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val paymentSummaries: List<PaymentSummaryEntity>,
    @Relation(parentColumn = "localId", entityColumn = "serviceId")
    val invoices: List<InvoiceEntity>,
)

@Dao
interface ServiceManagerDao {
    @Query(
        """
        SELECT s.localId, s.serviceCode, s.status, s.priority, s.updatedAt,
               COALESCE(MIN(n.createdAt), s.updatedAt) AS createdAt,
               c.name AS customerName, d.type, d.brand, d.model
        FROM service_orders s
        INNER JOIN customers c ON c.serviceId = s.localId
        INNER JOIN devices d ON d.serviceId = s.localId
        LEFT JOIN service_notes n ON n.serviceId = s.localId AND n.deletedAt IS NULL
        WHERE s.deletedAt IS NULL
        GROUP BY s.localId, s.serviceCode, s.status, s.priority, s.updatedAt, c.name, d.type, d.brand, d.model
        ORDER BY s.updatedAt DESC
        """,
    )
    fun observeServiceSummaries(): Flow<List<ServiceSummaryProjection>>

    @Transaction
    @Query("SELECT * FROM service_orders WHERE localId = :serviceId LIMIT 1")
    fun observeServiceAggregate(serviceId: Long): Flow<ServiceAggregate?>

    @Query("SELECT * FROM service_orders WHERE localId = :serviceId LIMIT 1")
    suspend fun getServiceOrder(serviceId: Long): ServiceOrderEntity?

    @Query("SELECT COUNT(*) FROM service_orders")
    suspend fun getServiceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceOrder(serviceOrder: ServiceOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssues(issues: List<IssueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSparePart(part: SparePartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosticSession(session: DiagnosticSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusTransition(transition: StatusTransitionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEntries(entries: List<WorkflowTimelineEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEntry(entry: WorkflowTimelineEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceNote(note: ServiceNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: TechnicianAssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentSummary(paymentSummary: PaymentSummaryEntity)

    @Query("SELECT DISTINCT name, phone, type FROM customers WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' OR phone LIKE '%' || :query || '%'")
    suspend fun searchCustomers(query: String): List<CustomerSearchProjection>

    @Query("SELECT DISTINCT name, phone, type FROM customers ORDER BY name ASC")
    fun observeDistinctCustomers(): Flow<List<CustomerDirectoryProjection>>

    @Query("SELECT * FROM customer_profiles ORDER BY name ASC")
    fun observeCustomerProfiles(): Flow<List<CustomerProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerProfile(profile: CustomerProfileEntity): Long

    @Query("SELECT * FROM customer_profiles WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' OR phone LIKE '%' || :query || '%'")
    suspend fun searchCustomerProfiles(query: String): List<CustomerProfileEntity>

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int

    @Query("SELECT name FROM customers LIMIT 10")
    suspend fun getAllCustomerNames(): List<String>

    @Query("DELETE FROM workflow_timeline_entries WHERE serviceId = :serviceId")
    suspend fun clearTimeline(serviceId: Long)

    @Update
    suspend fun updateServiceOrder(serviceOrder: ServiceOrderEntity)

    @Query("SELECT * FROM status_configs")
    fun observeStatusConfigs(): Flow<List<StatusConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusConfig(config: StatusConfigEntity)

    @Query("SELECT * FROM status_configs WHERE status = :status")
    suspend fun getStatusConfig(status: ServiceStatus): StatusConfigEntity?

    @Query("SELECT COUNT(*) FROM status_configs")
    suspend fun getStatusConfigCount(): Int

    @Query("SELECT * FROM brands WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'")
    suspend fun searchBrands(query: String): List<BrandEntity>

    @Query("SELECT COUNT(*) FROM brands WHERE LOWER(name) = LOWER(:name)")
    suspend fun getBrandCountByName(name: String): Int

    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun observeBrands(): Flow<List<BrandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: BrandEntity): Long

    @Query("DELETE FROM brands WHERE name = :name")
    suspend fun deleteBrandByName(name: String)

    @Query("SELECT COUNT(*) FROM brands")
    suspend fun getBrandCount(): Int

    @Query("SELECT * FROM device_types ORDER BY name ASC")
    fun observeDeviceTypes(): Flow<List<DeviceTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceType(deviceType: DeviceTypeEntity): Long

    @Query("SELECT COUNT(*) FROM device_types WHERE LOWER(name) = LOWER(:name)")
    suspend fun getDeviceTypeCountByName(name: String): Int

    @Query("DELETE FROM device_types WHERE name = :name")
    suspend fun deleteDeviceTypeByName(name: String)

    @Query("SELECT COUNT(*) FROM device_types")
    suspend fun getDeviceTypeCount(): Int

    @Query("SELECT * FROM qc_checklist ORDER BY title ASC")
    fun observeQCChecklist(): Flow<List<QCChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQCChecklistItem(item: QCChecklistItemEntity): Long

    @Query("DELETE FROM qc_checklist WHERE localId = :id")
    suspend fun deleteQCChecklistItem(id: Long)

    @Query("SELECT COUNT(*) FROM qc_checklist")
    suspend fun getQCChecklistCount(): Int

    @Query("SELECT COUNT(*) FROM qc_checklist WHERE LOWER(title) = LOWER(:title)")
    suspend fun getQCChecklistCountByTitle(title: String): Int

    @Query(
        """
        SELECT i.*, s.serviceCode, c.name as customerName 
        FROM invoices i
        JOIN service_orders s ON i.serviceId = s.localId
        JOIN customers c ON c.serviceId = s.localId
        ORDER BY i.createdAt DESC
        """,
    )
    fun observeInvoices(): Flow<List<InvoiceProjection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("SELECT * FROM invoices WHERE localId = :id")
    suspend fun getInvoice(id: Long): InvoiceEntity?

    @Query("DELETE FROM invoices WHERE localId = :id")
    suspend fun deleteInvoice(id: Long)
}

data class InvoiceProjection(
    val localId: Long,
    val serviceId: Long,
    val amount: Double,
    val status: com.example.servicemanager.core.domain.InvoiceStatus,
    val createdAt: Long,
    val serviceCode: String,
    val customerName: String,
)

@Database(
    entities = [
        ServiceOrderEntity::class,
        CustomerEntity::class,
        CustomerProfileEntity::class,
        DeviceEntity::class,
        IssueEntity::class,
        SparePartEntity::class,
        DiagnosticSessionEntity::class,
        StatusTransitionEntity::class,
        WorkflowTimelineEntryEntity::class,
        ServiceNoteEntity::class,
        TechnicianAssignmentEntity::class,
        PaymentSummaryEntity::class,
        InvoiceEntity::class,
        StatusConfigEntity::class,
        BrandEntity::class,
        DeviceTypeEntity::class,
        QCChecklistItemEntity::class,
    ],
    version = 9,
    exportSchema = false,
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceManagerDao(): ServiceManagerDao
}

class PreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val sortKey = stringPreferencesKey("service_sort_mode")
    private val statusOrderKey = stringPreferencesKey("status_order")
    private val companyNameKey = stringPreferencesKey("company_name")
    private val companyAddressKey = stringPreferencesKey("company_address")
    private val companyPhoneKey = stringPreferencesKey("company_phone")
    private val companyEmailKey = stringPreferencesKey("company_email")
    private val companyTaxIdKey = stringPreferencesKey("company_tax_id")
    private val companyOtherDetailsKey = stringPreferencesKey("company_other_details")

    val sortMode: Flow<ServiceSortMode> = context.dataStore.data.map { prefs: Preferences ->
        prefs[sortKey]?.let(ServiceSortMode::valueOf) ?: ServiceSortMode.UPDATED_DESC
    }

    val statusOrder: Flow<List<ServiceStatus>> = context.dataStore.data.map { prefs: Preferences ->
        val raw = prefs[statusOrderKey]
        val parsed = raw
            ?.split(",")
            ?.mapNotNull { token ->
                runCatching { ServiceStatus.valueOf(token) }.getOrNull()
            }
            .orEmpty()
        val defaultOrder = ServiceStatus.values().toList()
        (parsed + defaultOrder).distinct()
    }

    val companyProfile: Flow<CompanyProfile> = context.dataStore.data.map { prefs: Preferences ->
        CompanyProfile(
            companyName = prefs[companyNameKey]?.takeIf { it.isNotBlank() } ?: "Sentinel HUB",
            address = prefs[companyAddressKey] ?: "",
            phone = prefs[companyPhoneKey] ?: "",
            email = prefs[companyEmailKey] ?: "",
            taxId = prefs[companyTaxIdKey] ?: "",
            otherDetails = prefs[companyOtherDetailsKey] ?: "",
        )
    }

    suspend fun setSortMode(mode: ServiceSortMode) {
        context.dataStore.edit { it[sortKey] = mode.name }
    }

    suspend fun setStatusOrder(order: List<ServiceStatus>) {
        val normalized = (order + ServiceStatus.values().toList()).distinct()
        context.dataStore.edit { prefs ->
            prefs[statusOrderKey] = normalized.joinToString(",") { it.name }
        }
    }

    suspend fun updateCompanyProfile(profile: CompanyProfile) {
        context.dataStore.edit { prefs ->
            prefs[companyNameKey] = profile.companyName
            prefs[companyAddressKey] = profile.address
            prefs[companyPhoneKey] = profile.phone
            prefs[companyEmailKey] = profile.email
            prefs[companyTaxIdKey] = profile.taxId
            prefs[companyOtherDetailsKey] = profile.otherDetails
        }
    }
}

class ServiceOrderRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: ServiceManagerDao,
    private val preferencesStore: PreferencesStore,
    @ApplicationContext private val context: Context,
) : ServiceOrderRepository, NotesRepository {

    private fun generateServiceCode(): String {
        val number = (1000..9999).random()
        val suffix = ('A'..'Z').random()
        return "SN-$number-$suffix"
    }

    override fun observeServiceBuckets(query: String): Flow<ServiceBuckets> =
        dao.observeServiceSummaries().map { summaries ->
            val filtered = summaries
                .map {
                    ServiceSummary(
                        serviceId = it.localId,
                        serviceCode = it.serviceCode,
                        customerName = it.customerName,
                        deviceType = it.type,
                        deviceLabel = "${it.brand} ${it.model}",
                        status = it.status,
                        priority = it.priority,
                        bucket = it.status.toBucket(),
                        updatedAt = it.updatedAt,
                        createdAt = it.createdAt,
                    )
                }
                .filter { summary ->
                    query.isBlank() || listOf(
                        summary.serviceCode,
                        summary.customerName,
                        summary.deviceLabel,
                    ).any { token -> token.contains(query, ignoreCase = true) }
                }
            ServiceBuckets(
                inProgress = filtered.filter { it.bucket == LifecycleBucket.IN_PROGRESS },
                incoming = filtered.filter { it.bucket == LifecycleBucket.INCOMING },
                readyForPickup = filtered.filter { it.bucket == LifecycleBucket.READY_FOR_PICKUP },
                completed = filtered.filter { it.bucket == LifecycleBucket.COMPLETED },
            )
        }

    override fun observeServiceDetail(serviceId: Long): Flow<ServiceOrder?> =
        combine(
            dao.observeServiceAggregate(serviceId),
            dao.observeStatusConfigs()
        ) { aggregate, configs ->
            aggregate?.toDomain(configs.associateBy { it.status })
        }

    override fun observeStatusConfigs(): Flow<List<com.example.servicemanager.core.domain.ServiceStatusConfig>> =
        dao.observeStatusConfigs().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeStatusOrder(): Flow<List<ServiceStatus>> = preferencesStore.statusOrder

    override suspend fun updateStatusConfig(config: com.example.servicemanager.core.domain.ServiceStatusConfig) {
        dao.insertStatusConfig(
            StatusConfigEntity(
                status = config.status,
                estimatedMinutes = config.estimatedMinutes,
                fixedTimeOfDayMinutes = config.fixedTimeOfDayMinutes,
                showQcWarningForIncomplete = config.showQcWarningForIncomplete,
                isActive = config.isActive,
            )
        )
    }

    override suspend fun setStatusConfigActive(status: ServiceStatus, isActive: Boolean) {
        val existing = dao.getStatusConfig(status)
        val base = existing ?: StatusConfigEntity(
            status = status,
            estimatedMinutes = 0,
            fixedTimeOfDayMinutes = null,
            showQcWarningForIncomplete = false,
            isActive = true,
        )
        dao.insertStatusConfig(base.copy(isActive = isActive))
    }

    override suspend fun setStatusOrder(order: List<ServiceStatus>) {
        preferencesStore.setStatusOrder(order)
    }

    override suspend fun addServiceNote(serviceId: Long, note: String) {
        addNote(serviceId, note)
    }

    override suspend fun addServiceOrder(request: AddServiceOrderRequest): Result<Long> {
        val now = System.currentTimeMillis()
        val generatedServiceCode = generateServiceCode()
        var serviceId = 0L
        database.withTransaction {
            serviceId = dao.insertServiceOrder(
                ServiceOrderEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceCode = generatedServiceCode,
                    status = request.status,
                    priority = request.priority,
                    intent = request.intent,
                    reportedProblem = request.reportedProblem,
                    updatedAt = now,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            )
            dao.insertCustomer(
                CustomerEntity(
                    serviceId = serviceId,
                    name = request.customerName,
                    phone = request.customerPhone.ifBlank { "N/A" },
                    type = request.customerType,
                ),
            )
            dao.insertDevice(
                DeviceEntity(
                    serviceId = serviceId,
                    type = request.deviceType.ifBlank { "Device" },
                    brand = request.deviceBrand,
                    model = request.deviceModel,
                    serialNumber = request.serialNumber.ifBlank { generatedServiceCode },
                ),
            )
            dao.insertTimelineEntries(defaultTimeline(serviceId, request.status, hasIssue = false))
            dao.insertAssignment(
                TechnicianAssignmentEntity(
                    serviceId = serviceId,
                    technicianName = "Open Queue",
                    role = "Unassigned",
                ),
            )
            dao.insertPaymentSummary(
                PaymentSummaryEntity(
                    serviceId = serviceId,
                    initialEstimate = request.initialEstimate,
                    advancePaid = request.advancePaid,
                ),
            )
            dao.insertServiceNote(
                ServiceNoteEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = serviceId,
                    body = request.reportedProblem,
                    createdAt = now,
                    updatedAt = now,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            )
        }
        PendingServiceWidgetProvider.updateAllWidgets(context)
        return Result.success(serviceId)
    }

    override suspend fun searchCustomers(query: String): List<Customer> {
        val count = dao.getCustomerCount()
        val allNames = dao.getAllCustomerNames()
        val results = dao.searchCustomers(query.trim())
        val profileResults = dao.searchCustomerProfiles(query.trim())
        android.util.Log.d("ServiceOrderRepo", "Search query: '$query', Total customers: $count, Sample names: $allNames, Results found: ${results.size}")
        results.forEach {
            android.util.Log.d("ServiceOrderRepo", "Match: ${it.name} (${it.phone})")
        }

        return (results.map { Customer(name = it.name, phone = it.phone, type = it.type) } +
            profileResults.map { Customer(name = it.name, phone = it.phone, type = it.type) })
            .distinctBy { "${it.name.lowercase()}|${it.phone}" }
    }

    override fun observeCustomerProfiles(): Flow<List<com.example.servicemanager.core.domain.CustomerProfile>> =
        combine(
            dao.observeDistinctCustomers(),
            dao.observeCustomerProfiles(),
        ) { serviceCustomers, customerProfiles ->
            val fromServices = serviceCustomers.map {
                com.example.servicemanager.core.domain.CustomerProfile(
                    name = it.name,
                    phone = it.phone,
                    type = it.type,
                )
            }
            val fromProfiles = customerProfiles.map {
                com.example.servicemanager.core.domain.CustomerProfile(
                    id = it.localId,
                    name = it.name,
                    phone = it.phone,
                    email = it.email,
                    company = it.company,
                    address = it.address,
                    type = it.type,
                )
            }
            (fromServices + fromProfiles)
                .distinctBy { "${it.name.lowercase()}|${it.phone}" }
                .sortedBy { it.name.lowercase() }
        }

    override suspend fun addCustomerProfile(profile: com.example.servicemanager.core.domain.CustomerProfile) {
        dao.insertCustomerProfile(
            CustomerProfileEntity(
                name = profile.name,
                phone = profile.phone,
                email = profile.email,
                company = profile.company,
                address = profile.address,
                type = profile.type,
            )
        )
    }

    override suspend fun searchBrands(query: String): List<com.example.servicemanager.core.domain.Brand> {
        return dao.searchBrands(query.trim()).map {
            com.example.servicemanager.core.domain.Brand(name = it.name, logoUrl = it.logoUrl)
        }
    }

    override fun observeBrands(): Flow<List<com.example.servicemanager.core.domain.Brand>> =
        dao.observeBrands().map { entities ->
            entities.map { com.example.servicemanager.core.domain.Brand(name = it.name, logoUrl = it.logoUrl) }
        }

    override suspend fun addBrand(brand: com.example.servicemanager.core.domain.Brand) {
        dao.insertBrand(BrandEntity(name = brand.name, logoUrl = brand.logoUrl))
    }

    override suspend fun deleteBrand(brand: com.example.servicemanager.core.domain.Brand) {
        dao.deleteBrandByName(brand.name)
    }

    override fun observeDeviceTypes(): Flow<List<com.example.servicemanager.core.domain.DeviceType>> =
        dao.observeDeviceTypes().map { entities ->
            entities.map { com.example.servicemanager.core.domain.DeviceType(name = it.name, iconName = it.iconName) }
        }

    override suspend fun addDeviceType(deviceType: com.example.servicemanager.core.domain.DeviceType) {
        dao.insertDeviceType(DeviceTypeEntity(name = deviceType.name, iconName = deviceType.iconName))
    }

    override suspend fun deleteDeviceType(deviceType: com.example.servicemanager.core.domain.DeviceType) {
        dao.deleteDeviceTypeByName(deviceType.name)
    }

    override fun observeQCChecklist(): Flow<List<com.example.servicemanager.core.domain.QCChecklistItem>> =
        dao.observeQCChecklist().map { entities ->
            entities.map { com.example.servicemanager.core.domain.QCChecklistItem(id = it.localId, title = it.title, description = it.description) }
        }

    override suspend fun addQCChecklistItem(item: com.example.servicemanager.core.domain.QCChecklistItem) {
        dao.insertQCChecklistItem(QCChecklistItemEntity(title = item.title, description = item.description))
    }

    override suspend fun deleteQCChecklistItem(item: com.example.servicemanager.core.domain.QCChecklistItem) {
        dao.deleteQCChecklistItem(item.id)
    }

    override suspend fun seedIfEmpty() {
        database.withTransaction {
            if (dao.getServiceCount() == 0) {
                SeedData.services.forEach { seed ->
                    val serviceId = dao.insertServiceOrder(seed.serviceOrder)
                    dao.insertCustomer(seed.customer.copy(serviceId = serviceId))
                    dao.insertDevice(seed.device.copy(serviceId = serviceId))
                    dao.insertIssues(seed.issues.map { it.copy(serviceId = serviceId) })
                    seed.parts.forEach { dao.insertSparePart(it.copy(serviceId = serviceId)) }
                    seed.diagnostics.forEach { dao.insertDiagnosticSession(it.copy(serviceId = serviceId)) }
                    seed.transitions.forEach { dao.insertStatusTransition(it.copy(serviceId = serviceId)) }
                    dao.insertTimelineEntries(seed.timeline.map { it.copy(serviceId = serviceId) })
                    seed.notes.forEach { dao.insertServiceNote(it.copy(serviceId = serviceId) ) }
                    dao.insertAssignment(seed.assignment.copy(serviceId = serviceId))
                    dao.insertPaymentSummary(seed.payment.copy(serviceId = serviceId))
                }
            }

            if (dao.getBrandCount() == 0) {
                SeedData.brands.forEach { dao.insertBrand(it) }
            }
            SeedData.mobileBrands.forEach { brand ->
                if (dao.getBrandCountByName(brand.name) == 0) {
                    dao.insertBrand(brand)
                }
            }
            if (dao.getDeviceTypeCount() == 0) {
                SeedData.deviceTypes.forEach { dao.insertDeviceType(it) }
            }
            SeedData.mobileDeviceTypes.forEach { deviceType ->
                if (dao.getDeviceTypeCountByName(deviceType.name) == 0) {
                    dao.insertDeviceType(deviceType)
                }
            }
            if (dao.getStatusConfigCount() == 0) {
                SeedData.defaultStatusConfigs.forEach { dao.insertStatusConfig(it) }
            }
            if (dao.getQCChecklistCount() == 0) {
                SeedData.mobileQcChecklist.forEach { dao.insertQCChecklistItem(it) }
            } else {
                SeedData.mobileQcChecklist.forEach { item ->
                    if (dao.getQCChecklistCountByTitle(item.title) == 0) {
                        dao.insertQCChecklistItem(item)
                    }
                }
            }
        }
    }

    override suspend fun addNote(serviceId: Long, note: String): Result<Unit> {
        val now = System.currentTimeMillis()
        database.withTransaction {
            dao.insertServiceNote(
                ServiceNoteEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = serviceId,
                    body = note,
                    createdAt = now,
                    updatedAt = now,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            )
            dao.getServiceOrder(serviceId)?.let { dao.updateServiceOrder(it.copy(updatedAt = now)) }
        }
        PendingServiceWidgetProvider.updateAllWidgets(context)
        return Result.success(Unit)
    }
}

class DiagnosticsRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: ServiceManagerDao,
    @ApplicationContext private val context: Context,
) : DiagnosticsRepository {
    override suspend fun runDiagnostics(serviceId: Long, request: RunDiagnosticsRequest): Result<Unit> {
        val now = System.currentTimeMillis()
        database.withTransaction {
            dao.insertDiagnosticSession(
                DiagnosticSessionEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = serviceId,
                    primaryPowerRailConnection = request.primaryPowerRailConnection,
                    dataLinkHandshake = request.dataLinkHandshake,
                    internalSensorCalibration = request.internalSensorCalibration,
                    qcTotal = request.qcTotal,
                    qcPassed = request.qcPassed,
                    qcFailed = request.qcFailed,
                    createdAt = now,
                    updatedAt = now,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            )
            dao.getServiceOrder(serviceId)?.let { service ->
                dao.updateServiceOrder(
                    service.copy(
                        status = ServiceStatus.DIAGNOSTICS,
                        updatedAt = now,
                        syncState = SyncState.LOCAL_ONLY,
                    ),
                )
                dao.insertStatusTransition(
                    StatusTransitionEntity(
                        serviceId = serviceId,
                        fromStatus = service.status,
                        toStatus = ServiceStatus.DIAGNOSTICS,
                        note = "Diagnostics report submitted.",
                        createdAt = now,
                    ),
                )
                rebuildTimeline(serviceId, ServiceStatus.DIAGNOSTICS, request.qcFailed > 0)
            }
        }
        PendingServiceWidgetProvider.updateAllWidgets(context)
        return Result.success(Unit)
    }

    private suspend fun rebuildTimeline(serviceId: Long, status: ServiceStatus, hasIssue: Boolean) {
        dao.clearTimeline(serviceId)
        dao.insertTimelineEntries(defaultTimeline(serviceId, status, hasIssue))
    }
}

class SparePartRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: ServiceManagerDao,
    @ApplicationContext private val context: Context,
) : SparePartRepository {
    override suspend fun addSparePart(serviceId: Long, request: AddSparePartRequest): Result<Unit> {
        val now = System.currentTimeMillis()
        database.withTransaction {
            dao.insertSparePart(
                SparePartEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = serviceId,
                    name = request.name,
                    storageBoxNumber = request.storageBoxNumber,
                    assignedStation = request.assignedStation,
                    inventoryLevel = request.inventoryLevel,
                    updatedAt = now,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            )
            dao.insertServiceNote(
                ServiceNoteEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = serviceId,
                    body = "Spare part added: ${request.name} (${request.storageBoxNumber}).",
                    createdAt = now,
                    updatedAt = now,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            )
            dao.getServiceOrder(serviceId)?.let { dao.updateServiceOrder(it.copy(updatedAt = now)) }
        }
        PendingServiceWidgetProvider.updateAllWidgets(context)
        return Result.success(Unit)
    }
}

class StatusWorkflowRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: ServiceManagerDao,
    @ApplicationContext private val context: Context,
) : StatusWorkflowRepository {
    override suspend fun updateStatus(serviceId: Long, request: UpdateStatusRequest): Result<Unit> {
        val service = dao.getServiceOrder(serviceId) ?: return Result.failure(
            IllegalArgumentException("Service not found."),
        )
        val now = System.currentTimeMillis()
        database.withTransaction {
            dao.updateServiceOrder(
                service.copy(
                    status = request.targetStatus,
                    updatedAt = now,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            )
            dao.insertStatusTransition(
                StatusTransitionEntity(
                    serviceId = serviceId,
                    fromStatus = service.status,
                    toStatus = request.targetStatus,
                    note = request.note.ifBlank { "Status updated to ${request.targetStatus.name}." },
                    createdAt = now,
                ),
            )
            if (request.note.isNotBlank()) {
                dao.insertServiceNote(
                    ServiceNoteEntity(
                        uuid = UUID.randomUUID().toString(),
                        serviceId = serviceId,
                        body = request.note,
                        createdAt = now,
                        updatedAt = now,
                        syncState = SyncState.LOCAL_ONLY,
                    ),
                )
            }
            dao.clearTimeline(serviceId)
            dao.insertTimelineEntries(defaultTimeline(serviceId, request.targetStatus, hasIssue = false))
        }
        PendingServiceWidgetProvider.updateAllWidgets(context)
        return Result.success(Unit)
    }
}

private fun StatusConfigEntity.toDomain() =
    com.example.servicemanager.core.domain.ServiceStatusConfig(
        status = status,
        estimatedMinutes = estimatedMinutes,
        fixedTimeOfDayMinutes = fixedTimeOfDayMinutes,
        showQcWarningForIncomplete = showQcWarningForIncomplete,
        isActive = isActive,
    )

private fun calculateExpectedTime(status: ServiceStatus, updatedAt: Long, configs: Map<ServiceStatus, StatusConfigEntity>): Long? {
    val config = configs[status] ?: return null
    if (!config.isActive) return null
    return if (config.fixedTimeOfDayMinutes != null) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = updatedAt
        val updateMinutes = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)
        
        if (updateMinutes >= config.fixedTimeOfDayMinutes) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        calendar.set(java.util.Calendar.HOUR_OF_DAY, config.fixedTimeOfDayMinutes / 60)
        calendar.set(java.util.Calendar.MINUTE, config.fixedTimeOfDayMinutes % 60)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    } else {
        updatedAt + (config.estimatedMinutes * 60 * 1000L)
    }
}

private fun ServiceAggregate.toDomain(configs: Map<ServiceStatus, StatusConfigEntity>): ServiceOrder {
    val expectedTime = calculateExpectedTime(service.status, service.updatedAt, configs)
    return ServiceOrder(
        sync = service.toSync(),
        serviceCode = service.serviceCode,
        status = service.status,
        priority = service.priority,
        intent = service.intent,
        reportedProblem = service.reportedProblem,
        customer = customer.first().toDomain(),
        device = device.first().toDomain(),
        issues = issues.map { it.toDomain() },
        spareParts = spareParts.sortedBy { it.localId }.map { it.toDomain() },
        diagnostics = diagnostics.sortedByDescending { it.createdAt }.map { it.toDomain() },
        transitions = transitions.sortedByDescending { it.createdAt }.map { it.toDomain() },
        timeline = timeline.sortedBy { it.localId }.map { it.toDomain() },
        notes = notes.sortedByDescending { it.createdAt }.map { it.toDomain() },
        assignment = assignments.first().toDomain(),
        payment = paymentSummaries.first().toDomain(),
        invoices = invoices.map { it.toDomain(service.serviceCode, customer.first().name) },
        expectedCompletionTime = expectedTime
    )
}

private fun InvoiceEntity.toDomain(serviceCode: String, customerName: String) = com.example.servicemanager.core.domain.Invoice(
    id = localId,
    serviceId = serviceId,
    serviceCode = serviceCode,
    customerName = customerName,
    amount = amount,
    status = status,
    createdAt = createdAt
)

private fun ServiceOrderEntity.toSync(): SyncMetadata =
    SyncMetadata(
        localId = localId,
        uuid = uuid,
        updatedAt = updatedAt,
        syncState = syncState,
        deletedAt = deletedAt,
    )

private fun SparePartEntity.toSync(): SyncMetadata =
    SyncMetadata(localId, uuid, updatedAt, syncState, deletedAt)

private fun DiagnosticSessionEntity.toSync(): SyncMetadata =
    SyncMetadata(localId, uuid, updatedAt, syncState, deletedAt)

private fun ServiceNoteEntity.toSync(): SyncMetadata =
    SyncMetadata(localId, uuid, updatedAt, syncState, deletedAt)

private fun CustomerEntity.toDomain() = Customer(name = name, phone = phone, type = type)
private fun DeviceEntity.toDomain() = com.example.servicemanager.core.domain.Device(type, brand, model, serialNumber)
private fun IssueEntity.toDomain() = Issue(localId, title, requirement)
private fun SparePartEntity.toDomain() = SparePart(toSync(), serviceId, name, storageBoxNumber, assignedStation, inventoryLevel)
private fun DiagnosticSessionEntity.toDomain() = DiagnosticSession(
    sync = toSync(),
    serviceId = serviceId,
    primaryPowerRailConnection = primaryPowerRailConnection,
    dataLinkHandshake = dataLinkHandshake,
    internalSensorCalibration = internalSensorCalibration,
    qcTotal = qcTotal,
    qcPassed = qcPassed,
    qcFailed = qcFailed,
    createdAt = createdAt,
)
private fun StatusTransitionEntity.toDomain() = StatusTransition(localId, serviceId, fromStatus, toStatus, note, createdAt)
private fun WorkflowTimelineEntryEntity.toDomain() = WorkflowTimelineEntry(localId, serviceId, title, subtitle, state)
private fun ServiceNoteEntity.toDomain() = ServiceNote(toSync(), serviceId, body, createdAt)
private fun TechnicianAssignmentEntity.toDomain() = TechnicianAssignment(technicianName, role)
private fun PaymentSummaryEntity.toDomain() = PaymentSummary(initialEstimate, advancePaid)

private fun defaultTimeline(
    serviceId: Long,
    status: ServiceStatus,
    hasIssue: Boolean,
): List<WorkflowTimelineEntryEntity> {
    val diagnosisState = when (status) {
        ServiceStatus.QUEUED -> TimelineState.PENDING
        ServiceStatus.IN_PROGRESS -> TimelineState.ACTIVE
        ServiceStatus.DIAGNOSTICS, ServiceStatus.WAITING_FOR_SPARE, ServiceStatus.READY_FOR_PICKUP, ServiceStatus.COMPLETED -> TimelineState.DONE
        ServiceStatus.CANCELLED -> TimelineState.PENDING
    }
    val partsState = when (status) {
        ServiceStatus.QUEUED -> TimelineState.PENDING
        ServiceStatus.IN_PROGRESS, ServiceStatus.DIAGNOSTICS, ServiceStatus.WAITING_FOR_SPARE -> TimelineState.ACTIVE
        ServiceStatus.READY_FOR_PICKUP, ServiceStatus.COMPLETED -> TimelineState.DONE
        ServiceStatus.CANCELLED -> TimelineState.PENDING
    }
    val qaState = when (status) {
        ServiceStatus.READY_FOR_PICKUP, ServiceStatus.COMPLETED -> TimelineState.DONE
        ServiceStatus.CANCELLED -> TimelineState.PENDING
        else -> if (hasIssue) TimelineState.ACTIVE else TimelineState.PENDING
    }
    return listOf(
        WorkflowTimelineEntryEntity(
            serviceId = serviceId,
            title = "Initial Diagnostics",
            subtitle = "Editorial intake sequence completed",
            state = diagnosisState,
        ),
        WorkflowTimelineEntryEntity(
            serviceId = serviceId,
            title = "Parts Arrival",
            subtitle = "Storage reconciliation in progress",
            state = partsState,
        ),
        WorkflowTimelineEntryEntity(
            serviceId = serviceId,
            title = "Final Testing",
            subtitle = if (hasIssue) "Pending completion" else "Ready for handoff",
            state = qaState,
        ),
    )
}

private data class SeedBundle(
    val serviceOrder: ServiceOrderEntity,
    val customer: CustomerEntity,
    val device: DeviceEntity,
    val issues: List<IssueEntity>,
    val parts: List<SparePartEntity>,
    val diagnostics: List<DiagnosticSessionEntity>,
    val transitions: List<StatusTransitionEntity>,
    val timeline: List<WorkflowTimelineEntryEntity>,
    val notes: List<ServiceNoteEntity>,
    val assignment: TechnicianAssignmentEntity,
    val payment: PaymentSummaryEntity,
)

private object SeedData {
    private val now = System.currentTimeMillis()

    val services = listOf(
        SeedBundle(
            serviceOrder = ServiceOrderEntity(
                uuid = UUID.randomUUID().toString(),
                serviceCode = "SN-8829-X",
                status = ServiceStatus.IN_PROGRESS,
                priority = PriorityLevel.HIGH,
                intent = "Logic Board Repair",
                reportedProblem = "Device intermittently loses power under heavy load.",
                updatedAt = now - 1_000L,
                syncState = SyncState.LOCAL_ONLY,
            ),
            customer = CustomerEntity(serviceId = 0, name = "Marcus Holloway", phone = "+91 98400 11011", type = "Individual"),
            device = DeviceEntity(serviceId = 0, type = "Laptop", brand = "MacBook Pro", model = "M2", serialNumber = "SN-8829-AQ"),
            issues = listOf(
                IssueEntity(serviceId = 0, title = "Compressor Valve Leak", requirement = "Requires replacement gasket set #G44"),
                IssueEntity(serviceId = 0, title = "Fan Motor Degradation", requirement = "Requires 12V high torque motor"),
            ),
            parts = listOf(
                SparePartEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = 0,
                    name = "M4 Hex Bolt (24pcs)",
                    storageBoxNumber = "BOX-44-DELTA",
                    assignedStation = "STATION-04-B",
                    inventoryLevel = "AUTOMATIC",
                    updatedAt = now,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            ),
            diagnostics = listOf(
                DiagnosticSessionEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = 0,
                    primaryPowerRailConnection = DiagnosticAnswer.YES,
                    dataLinkHandshake = DiagnosticAnswer.NOT_TESTED,
                    internalSensorCalibration = DiagnosticAnswer.NO,
                    qcTotal = 12,
                    qcPassed = 10,
                    qcFailed = 2,
                    createdAt = now - 100_000L,
                    updatedAt = now - 100_000L,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            ),
            transitions = listOf(
                StatusTransitionEntity(serviceId = 0, fromStatus = ServiceStatus.QUEUED, toStatus = ServiceStatus.IN_PROGRESS, note = "Service intake approved.", createdAt = now - 250_000L),
            ),
            timeline = defaultTimeline(serviceId = 0, status = ServiceStatus.IN_PROGRESS, hasIssue = true),
            notes = listOf(
                ServiceNoteEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = 0,
                    body = "Customer requested a call 30 minutes prior to technician arrival.",
                    createdAt = now - 80_000L,
                    updatedAt = now - 80_000L,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            ),
            assignment = TechnicianAssignmentEntity(serviceId = 0, technicianName = "Kailash (Lead Field Tech)", role = "Lead Field Tech"),
            payment = PaymentSummaryEntity(serviceId = 0, initialEstimate = 1450.0, advancePaid = 500.0),
        ),
        SeedBundle(
            serviceOrder = ServiceOrderEntity(
                uuid = UUID.randomUUID().toString(),
                serviceCode = "SN-9001-A",
                status = ServiceStatus.IN_PROGRESS,
                priority = PriorityLevel.MEDIUM,
                intent = "Sensor Cleaning",
                reportedProblem = "Sensor dust accumulation degrading image quality.",
                updatedAt = now - 2_000L,
                syncState = SyncState.LOCAL_ONLY,
            ),
            customer = CustomerEntity(serviceId = 0, name = "Elena Fisher", phone = "+91 98400 22022", type = "Individual"),
            device = DeviceEntity(serviceId = 0, type = "Camera", brand = "Sony A7R IV", model = "Sensor Cleaning", serialNumber = "CAM-8844"),
            issues = listOf(IssueEntity(serviceId = 0, title = "Optics contamination", requirement = "Precision bench cleaning")),
            parts = emptyList(),
            diagnostics = emptyList(),
            transitions = listOf(StatusTransitionEntity(serviceId = 0, fromStatus = ServiceStatus.QUEUED, toStatus = ServiceStatus.IN_PROGRESS, note = "Technician assigned.", createdAt = now - 150_000L)),
            timeline = defaultTimeline(serviceId = 0, status = ServiceStatus.IN_PROGRESS, hasIssue = false),
            notes = emptyList(),
            assignment = TechnicianAssignmentEntity(serviceId = 0, technicianName = "Asha Menon", role = "Bench Specialist"),
            payment = PaymentSummaryEntity(serviceId = 0, initialEstimate = 320.0, advancePaid = 120.0),
        ),
        SeedBundle(
            serviceOrder = ServiceOrderEntity(
                uuid = UUID.randomUUID().toString(),
                serviceCode = "SN-9142-W",
                status = ServiceStatus.QUEUED,
                priority = PriorityLevel.LOW,
                intent = "Calibration",
                reportedProblem = "Watch requires timing calibration and gaskets check.",
                updatedAt = now - 3_000L,
                syncState = SyncState.LOCAL_ONLY,
            ),
            customer = CustomerEntity(serviceId = 0, name = "Arthur Morgan", phone = "+91 98400 33033", type = "Individual"),
            device = DeviceEntity(serviceId = 0, type = "Watch", brand = "Tag Heuer Monaco", model = "Calibrating", serialNumber = "MONACO-77"),
            issues = listOf(IssueEntity(serviceId = 0, title = "Timing drift", requirement = "Precision calibration cycle")),
            parts = emptyList(),
            diagnostics = emptyList(),
            transitions = emptyList(),
            timeline = defaultTimeline(serviceId = 0, status = ServiceStatus.QUEUED, hasIssue = false),
            notes = emptyList(),
            assignment = TechnicianAssignmentEntity(serviceId = 0, technicianName = "Open Queue", role = "Unassigned"),
            payment = PaymentSummaryEntity(serviceId = 0, initialEstimate = 210.0, advancePaid = 0.0),
        ),
        SeedBundle(
            serviceOrder = ServiceOrderEntity(
                uuid = UUID.randomUUID().toString(),
                serviceCode = "SN-8755-Z",
                status = ServiceStatus.READY_FOR_PICKUP,
                priority = PriorityLevel.MEDIUM,
                intent = "Battery Replacement",
                reportedProblem = "Tablet battery drains rapidly and shuts down at 20%.",
                updatedAt = now - 4_000L,
                syncState = SyncState.LOCAL_ONLY,
            ),
            customer = CustomerEntity(serviceId = 0, name = "Lara Croft", phone = "+91 98400 44044", type = "Individual"),
            device = DeviceEntity(serviceId = 0, type = "Tablet", brand = "iPad Pro 12.9", model = "Battery Replacement", serialNumber = "IPD-9982"),
            issues = listOf(IssueEntity(serviceId = 0, title = "Battery swell risk", requirement = "Replace and calibrate power cell")),
            parts = emptyList(),
            diagnostics = listOf(
                DiagnosticSessionEntity(
                    uuid = UUID.randomUUID().toString(),
                    serviceId = 0,
                    primaryPowerRailConnection = DiagnosticAnswer.YES,
                    dataLinkHandshake = DiagnosticAnswer.YES,
                    internalSensorCalibration = DiagnosticAnswer.YES,
                    qcTotal = 8,
                    qcPassed = 8,
                    qcFailed = 0,
                    createdAt = now - 40_000L,
                    updatedAt = now - 40_000L,
                    syncState = SyncState.LOCAL_ONLY,
                ),
            ),
            transitions = listOf(StatusTransitionEntity(serviceId = 0, fromStatus = ServiceStatus.IN_PROGRESS, toStatus = ServiceStatus.READY_FOR_PICKUP, note = "Final QA cleared.", createdAt = now - 30_000L)),
            timeline = defaultTimeline(serviceId = 0, status = ServiceStatus.READY_FOR_PICKUP, hasIssue = false),
            notes = emptyList(),
            assignment = TechnicianAssignmentEntity(serviceId = 0, technicianName = "Kiran Joseph", role = "Pickup Coordinator"),
            payment = PaymentSummaryEntity(serviceId = 0, initialEstimate = 580.0, advancePaid = 580.0),
        ),
        SeedBundle(
            serviceOrder = ServiceOrderEntity(
                uuid = UUID.randomUUID().toString(),
                serviceCode = "SN-1024-K",
                status = ServiceStatus.QUEUED,
                priority = PriorityLevel.HIGH,
                intent = "Quick Diagnostic",
                reportedProblem = "Testing search functionality with short names.",
                updatedAt = now - 5_000L,
                syncState = SyncState.LOCAL_ONLY,
            ),
            customer = CustomerEntity(serviceId = 0, name = "Kailash", phone = "+91 99999 88888", type = "Individual"),
            device = DeviceEntity(serviceId = 0, type = "Phone", brand = "Pixel", model = "8 Pro", serialNumber = "P8P-001"),
            issues = emptyList(),
            parts = emptyList(),
            diagnostics = emptyList(),
            transitions = emptyList(),
            timeline = defaultTimeline(serviceId = 0, status = ServiceStatus.QUEUED, hasIssue = false),
            notes = emptyList(),
            assignment = TechnicianAssignmentEntity(serviceId = 0, technicianName = "Open Queue", role = "Unassigned"),
            payment = PaymentSummaryEntity(serviceId = 0, initialEstimate = 0.0, advancePaid = 0.0),
        ),
    )

    val brands = listOf(
        BrandEntity(name = "Apple"),
        BrandEntity(name = "Samsung"),
        BrandEntity(name = "Sony"),
        BrandEntity(name = "HP"),
        BrandEntity(name = "Dell"),
        BrandEntity(name = "Lenovo"),
        BrandEntity(name = "Asus"),
        BrandEntity(name = "Microsoft"),
        BrandEntity(name = "Logitech"),
        BrandEntity(name = "Canon"),
        BrandEntity(name = "Nikon"),
        BrandEntity(name = "DJI"),
        BrandEntity(name = "Rolex"),
        BrandEntity(name = "Tag Heuer"),
        BrandEntity(name = "Seiko"),
        BrandEntity(name = "Casio"),
    )

    val mobileBrands = listOf(
        BrandEntity(name = "Xiaomi"),
        BrandEntity(name = "Redmi"),
        BrandEntity(name = "POCO"),
        BrandEntity(name = "Realme"),
        BrandEntity(name = "OnePlus"),
        BrandEntity(name = "OPPO"),
        BrandEntity(name = "Vivo"),
        BrandEntity(name = "Motorola"),
        BrandEntity(name = "Nokia"),
        BrandEntity(name = "Google Pixel"),
        BrandEntity(name = "Tecno"),
        BrandEntity(name = "Infinix"),
        BrandEntity(name = "iQOO"),
        BrandEntity(name = "Nothing"),
        BrandEntity(name = "Lava"),
        BrandEntity(name = "Honor"),
        BrandEntity(name = "Huawei"),
        BrandEntity(name = "Asus ROG"),
        BrandEntity(name = "Sony Xperia"),
        BrandEntity(name = "Black Shark"),
    )

    val mobileQcChecklist = listOf(
        QCChecklistItemEntity(title = "Biometrics", description = "Fingerprint/face unlock works if available"),
        QCChecklistItemEntity(title = "Buttons", description = "Power and volume buttons function correctly"),
        QCChecklistItemEntity(title = "Display", description = "Touch, brightness, and dead-pixel check"),
        QCChecklistItemEntity(title = "Battery Health", description = "Charging and battery drain are normal"),
        QCChecklistItemEntity(title = "Camera Front/Back", description = "Front and rear cameras capture properly"),
        QCChecklistItemEntity(title = "Microphone & Speaker", description = "Call and media audio path validation"),
        QCChecklistItemEntity(title = "Wi-Fi / Bluetooth", description = "Wireless connectivity and pairing works"),
        QCChecklistItemEntity(title = "Network/SIM", description = "SIM detection and signal reception"),
        QCChecklistItemEntity(title = "Charging Port", description = "USB-C/Lightning charging and data link stable"),
        QCChecklistItemEntity(title = "Proximity / Auto-Brightness", description = "Proximity and ambient light sensors respond correctly"),
        QCChecklistItemEntity(title = "Vibration / Haptics", description = "Vibration motor feedback works without noise"),
        QCChecklistItemEntity(title = "Earpiece / Receiver", description = "In-call voice clarity on receiver path"),
        QCChecklistItemEntity(title = "GPS / Location", description = "Location lock and navigation accuracy check"),
    )

    val defaultStatusConfigs = ServiceStatus.values().map { status ->
        StatusConfigEntity(
            status = status,
            estimatedMinutes = 0,
            fixedTimeOfDayMinutes = null,
            showQcWarningForIncomplete = false,
            isActive = status != ServiceStatus.WAITING_FOR_SPARE,
        )
    }

    val deviceTypes = listOf(
        DeviceTypeEntity(name = "Laptop"),
        DeviceTypeEntity(name = "Phone"),
        DeviceTypeEntity(name = "Tablet"),
        DeviceTypeEntity(name = "Camera"),
        DeviceTypeEntity(name = "Watch"),
        DeviceTypeEntity(name = "Drone"),
        DeviceTypeEntity(name = "Console"),
        DeviceTypeEntity(name = "Other"),
    )

    val mobileDeviceTypes = listOf(
        DeviceTypeEntity(name = "Smartphone"),
        DeviceTypeEntity(name = "Feature Phone"),
        DeviceTypeEntity(name = "Foldable Phone"),
        DeviceTypeEntity(name = "Rugged Phone"),
        DeviceTypeEntity(name = "Gaming Phone"),
        DeviceTypeEntity(name = "Smartwatch"),
    )

}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "sentinel_manager.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideServiceManagerDao(database: AppDatabase): ServiceManagerDao = database.serviceManagerDao()

    @Provides
    @Singleton
    fun providePreferencesStore(@ApplicationContext context: Context): PreferencesStore = PreferencesStore(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindServiceOrderRepository(impl: ServiceOrderRepositoryImpl): ServiceOrderRepository

    @Binds
    abstract fun bindNotesRepository(impl: ServiceOrderRepositoryImpl): NotesRepository

    @Binds
    abstract fun bindDiagnosticsRepository(impl: DiagnosticsRepositoryImpl): DiagnosticsRepository

    @Binds
    abstract fun bindSparePartRepository(impl: SparePartRepositoryImpl): SparePartRepository

    @Binds
    abstract fun bindStatusWorkflowRepository(impl: StatusWorkflowRepositoryImpl): StatusWorkflowRepository

    @Binds
    abstract fun bindInvoiceRepository(impl: InvoiceRepositoryImpl): com.example.servicemanager.core.domain.InvoiceRepository
}

class InvoiceRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: ServiceManagerDao,
) : com.example.servicemanager.core.domain.InvoiceRepository {
    override fun observeInvoices(): Flow<List<com.example.servicemanager.core.domain.Invoice>> =
        dao.observeInvoices().map { projections ->
            projections.map {
                com.example.servicemanager.core.domain.Invoice(
                    id = it.localId,
                    serviceId = it.serviceId,
                    serviceCode = it.serviceCode,
                    customerName = it.customerName,
                    amount = it.amount,
                    status = it.status,
                    createdAt = it.createdAt,
                )
            }
        }

    override suspend fun createInvoice(serviceId: Long): Result<Long> {
        val aggregate = dao.observeServiceAggregate(serviceId).firstOrNull()
            ?: return Result.failure(Exception("Service not found"))
        
        val amount = aggregate.paymentSummaries.firstOrNull()?.initialEstimate ?: 0.0
        val invoiceId = dao.insertInvoice(
            InvoiceEntity(
                serviceId = serviceId,
                amount = amount,
                status = com.example.servicemanager.core.domain.InvoiceStatus.DRAFT,
                createdAt = System.currentTimeMillis()
            )
        )
        return Result.success(invoiceId)
    }

    override suspend fun updateInvoiceStatus(invoiceId: Long, status: com.example.servicemanager.core.domain.InvoiceStatus): Result<Unit> {
        val invoice = dao.getInvoice(invoiceId) ?: return Result.failure(Exception("Invoice not found"))
        dao.updateInvoice(invoice.copy(status = status))
        return Result.success(Unit)
    }

    override suspend fun deleteInvoice(invoiceId: Long): Result<Unit> {
        dao.deleteInvoice(invoiceId)
        return Result.success(Unit)
    }
}
