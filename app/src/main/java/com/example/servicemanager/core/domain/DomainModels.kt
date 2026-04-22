package com.example.servicemanager.core.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

enum class SyncState {
    LOCAL_ONLY,
    PENDING_UPLOAD,
    SYNCED,
    PENDING_DELETE,
}

enum class ServiceStatus {
    QUEUED,
    IN_PROGRESS,
    DIAGNOSTICS,
    WAITING_FOR_SPARE,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELLED,
}

enum class PriorityLevel {
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
}

enum class TimelineState {
    DONE,
    ACTIVE,
    PENDING,
}

enum class LifecycleBucket {
    IN_PROGRESS,
    INCOMING,
    READY_FOR_PICKUP,
    COMPLETED,
}

enum class InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    VOID,
}

data class Invoice(
    val id: Long,
    val serviceId: Long,
    val serviceCode: String,
    val customerName: String,
    val amount: Double,
    val status: InvoiceStatus,
    val createdAt: Long,
)

data class ServiceStatusConfig(
    val status: ServiceStatus,
    val estimatedMinutes: Int,
    val fixedTimeOfDayMinutes: Int? = null, // e.g., 600 for 10:00 AM
    val showQcWarningForIncomplete: Boolean = false,
    val isActive: Boolean = true,
)

enum class DiagnosticAnswer {
    YES,
    NO,
    NOT_TESTED,
}

data class SyncMetadata(
    val localId: Long,
    val uuid: String,
    val updatedAt: Long,
    val syncState: SyncState,
    val deletedAt: Long? = null,
)

data class Customer(
    val name: String,
    val phone: String,
    val type: String = "Individual",
)

data class CustomerProfile(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val company: String = "",
    val address: String = "",
    val type: String = "Individual",
)

data class Brand(
    val name: String,
    val logoUrl: String? = null,
)

data class DeviceType(
    val name: String,
    val iconName: String? = null,
)

data class QCChecklistItem(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
)

data class Device(
    val type: String,
    val brand: String,
    val model: String,
    val serialNumber: String,
)

data class Issue(
    val id: Long,
    val title: String,
    val requirement: String,
)

data class SparePart(
    val sync: SyncMetadata,
    val serviceId: Long,
    val name: String,
    val storageBoxNumber: String,
    val assignedStation: String,
    val inventoryLevel: String,
)

data class DiagnosticSession(
    val sync: SyncMetadata,
    val serviceId: Long,
    val primaryPowerRailConnection: DiagnosticAnswer,
    val dataLinkHandshake: DiagnosticAnswer,
    val internalSensorCalibration: DiagnosticAnswer,
    val qcTotal: Int,
    val qcPassed: Int,
    val qcFailed: Int,
    val createdAt: Long,
)

data class StatusTransition(
    val id: Long,
    val serviceId: Long,
    val fromStatus: ServiceStatus,
    val toStatus: ServiceStatus,
    val note: String,
    val createdAt: Long,
)

data class WorkflowTimelineEntry(
    val id: Long,
    val serviceId: Long,
    val title: String,
    val subtitle: String,
    val state: TimelineState,
)

data class ServiceNote(
    val sync: SyncMetadata,
    val serviceId: Long,
    val body: String,
    val createdAt: Long,
)

data class TechnicianAssignment(
    val technicianName: String,
    val role: String,
)

data class PaymentSummary(
    val initialEstimate: Double,
    val advancePaid: Double,
)

data class ServiceOrder(
    val sync: SyncMetadata,
    val serviceCode: String,
    val status: ServiceStatus,
    val priority: PriorityLevel,
    val intent: String,
    val reportedProblem: String,
    val customer: Customer,
    val device: Device,
    val issues: List<Issue>,
    val spareParts: List<SparePart>,
    val diagnostics: List<DiagnosticSession>,
    val transitions: List<StatusTransition>,
    val timeline: List<WorkflowTimelineEntry>,
    val notes: List<ServiceNote>,
    val assignment: TechnicianAssignment,
    val payment: PaymentSummary,
    val invoices: List<Invoice> = emptyList(),
    val expectedCompletionTime: Long? = null,
)

data class ServiceSummary(
    val serviceId: Long,
    val serviceCode: String,
    val customerName: String,
    val deviceType: String,
    val deviceLabel: String,
    val status: ServiceStatus,
    val priority: PriorityLevel,
    val bucket: LifecycleBucket,
    val updatedAt: Long,
    val createdAt: Long = updatedAt,
)

data class ServiceBuckets(
    val inProgress: List<ServiceSummary>,
    val incoming: List<ServiceSummary>,
    val readyForPickup: List<ServiceSummary>,
    val completed: List<ServiceSummary>,
)

data class RunDiagnosticsRequest(
    val primaryPowerRailConnection: DiagnosticAnswer,
    val dataLinkHandshake: DiagnosticAnswer,
    val internalSensorCalibration: DiagnosticAnswer,
    val qcTotal: Int,
    val qcPassed: Int,
    val qcFailed: Int,
)

data class AddSparePartRequest(
    val name: String,
    val storageBoxNumber: String,
    val assignedStation: String,
    val inventoryLevel: String,
)

data class UpdateStatusRequest(
    val targetStatus: ServiceStatus,
    val note: String,
)

data class AddServiceOrderRequest(
    val customerName: String,
    val customerPhone: String,
    val customerType: String,
    val deviceType: String,
    val deviceBrand: String,
    val deviceModel: String,
    val serialNumber: String,
    val intent: String,
    val reportedProblem: String,
    val status: ServiceStatus,
    val priority: PriorityLevel,
    val initialEstimate: Double = 0.0,
    val advancePaid: Double = 0.0,
)

interface ServiceOrderRepository {
    fun observeServiceBuckets(query: String): Flow<ServiceBuckets>
    fun observeServiceDetail(serviceId: Long): Flow<ServiceOrder?>
    suspend fun addServiceNote(serviceId: Long, note: String)
    suspend fun addServiceOrder(request: AddServiceOrderRequest): Result<Long>
    suspend fun seedIfEmpty()
    suspend fun searchCustomers(query: String): List<Customer>
    fun observeCustomerProfiles(): Flow<List<CustomerProfile>>
    suspend fun addCustomerProfile(profile: CustomerProfile)
    suspend fun searchBrands(query: String): List<Brand>
    fun observeBrands(): Flow<List<Brand>>
    suspend fun addBrand(brand: Brand)
    suspend fun deleteBrand(brand: Brand)
    fun observeDeviceTypes(): Flow<List<DeviceType>>
    suspend fun addDeviceType(deviceType: DeviceType)
    suspend fun deleteDeviceType(deviceType: DeviceType)
    fun observeStatusConfigs(): Flow<List<ServiceStatusConfig>>
    fun observeStatusOrder(): Flow<List<ServiceStatus>>
    suspend fun updateStatusConfig(config: ServiceStatusConfig)
    suspend fun setStatusConfigActive(status: ServiceStatus, isActive: Boolean)
    suspend fun setStatusOrder(order: List<ServiceStatus>)
    fun observeQCChecklist(): Flow<List<QCChecklistItem>>
    suspend fun addQCChecklistItem(item: QCChecklistItem)
    suspend fun deleteQCChecklistItem(item: QCChecklistItem)
}

interface DiagnosticsRepository {
    suspend fun runDiagnostics(serviceId: Long, request: RunDiagnosticsRequest): Result<Unit>
}

interface SparePartRepository {
    suspend fun addSparePart(serviceId: Long, request: AddSparePartRequest): Result<Unit>
}

interface NotesRepository {
    suspend fun addNote(serviceId: Long, note: String): Result<Unit>
}

interface StatusWorkflowRepository {
    suspend fun updateStatus(serviceId: Long, request: UpdateStatusRequest): Result<Unit>
}

interface InvoiceRepository {
    fun observeInvoices(): Flow<List<Invoice>>
    suspend fun createInvoice(serviceId: Long): Result<Long>
    suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit>
    suspend fun deleteInvoice(invoiceId: Long): Result<Unit>
}

fun ServiceStatus.toBucket(): LifecycleBucket =
    when (this) {
        ServiceStatus.QUEUED -> LifecycleBucket.INCOMING
        ServiceStatus.IN_PROGRESS, ServiceStatus.DIAGNOSTICS, ServiceStatus.WAITING_FOR_SPARE -> LifecycleBucket.IN_PROGRESS
        ServiceStatus.READY_FOR_PICKUP -> LifecycleBucket.READY_FOR_PICKUP
        ServiceStatus.COMPLETED, ServiceStatus.CANCELLED -> LifecycleBucket.COMPLETED
    }

class GetServiceBuckets @Inject constructor(
    private val repository: ServiceOrderRepository,
) {
    operator fun invoke(query: String): Flow<ServiceBuckets> = repository.observeServiceBuckets(query)
}

class SearchServices @Inject constructor(
    private val repository: ServiceOrderRepository,
) {
    operator fun invoke(query: String): Flow<ServiceBuckets> = repository.observeServiceBuckets(query)
}

class GetServiceDetail @Inject constructor(
    private val repository: ServiceOrderRepository,
) {
    operator fun invoke(serviceId: Long): Flow<ServiceOrder?> = repository.observeServiceDetail(serviceId)
}

class RunDiagnostics @Inject constructor(
    private val repository: DiagnosticsRepository,
) {
    suspend operator fun invoke(serviceId: Long, request: RunDiagnosticsRequest): Result<Unit> {
        val invalidCounts = request.qcPassed + request.qcFailed > request.qcTotal
        if (invalidCounts) {
            return Result.failure(IllegalArgumentException("QC passed and failed count cannot exceed total."))
        }
        return repository.runDiagnostics(serviceId, request)
    }
}

class AddSparePart @Inject constructor(
    private val repository: SparePartRepository,
) {
    suspend operator fun invoke(serviceId: Long, request: AddSparePartRequest): Result<Unit> {
        if (request.name.isBlank() || request.storageBoxNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Part name and storage box number are required."))
        }
        return repository.addSparePart(serviceId, request)
    }
}

class UpdateServiceStatus @Inject constructor(
    private val repository: StatusWorkflowRepository,
    private val createInvoice: CreateInvoice,
) {
    suspend operator fun invoke(
        serviceId: Long,
        currentService: ServiceOrder,
        request: UpdateStatusRequest,
    ): Result<Unit> {
        val latestDiagnostics = currentService.diagnostics.maxByOrNull { it.createdAt }
        val requiresNote = request.targetStatus == ServiceStatus.COMPLETED &&
            (
                latestDiagnostics?.internalSensorCalibration == DiagnosticAnswer.NO ||
                    (latestDiagnostics?.qcFailed ?: 0) > 0
                )
        if (requiresNote && request.note.isBlank()) {
            return Result.failure(
                IllegalArgumentException("A service note is required before completing a risky job."),
            )
        }
        val result = repository.updateStatus(serviceId, request)
        if (result.isSuccess && request.targetStatus == ServiceStatus.COMPLETED) {
            createInvoice(serviceId)
        }
        return result
    }
}

class AddServiceNote @Inject constructor(
    private val repository: NotesRepository,
) {
    suspend operator fun invoke(serviceId: Long, note: String): Result<Unit> {
        if (note.isBlank()) {
            return Result.failure(IllegalArgumentException("Note cannot be blank."))
        }
        return repository.addNote(serviceId, note)
    }
}

class AddServiceOrder @Inject constructor(
    private val repository: ServiceOrderRepository,
) {
    suspend operator fun invoke(request: AddServiceOrderRequest): Result<Long> {
        if (
            request.customerName.isBlank() ||
            request.deviceBrand.isBlank() ||
            request.deviceModel.isBlank() ||
            request.reportedProblem.isBlank()
        ) {
            return Result.failure(IllegalArgumentException("Fill customer, device, and issue details."))
        }
        return repository.addServiceOrder(request)
    }
}

class GetInvoices @Inject constructor(
    private val repository: InvoiceRepository,
) {
    operator fun invoke(): Flow<List<Invoice>> = repository.observeInvoices()
}

class CreateInvoice @Inject constructor(
    private val repository: InvoiceRepository,
) {
    suspend operator fun invoke(serviceId: Long): Result<Long> = repository.createInvoice(serviceId)
}

class DeleteInvoice @Inject constructor(
    private val repository: InvoiceRepository,
) {
    suspend operator fun invoke(invoiceId: Long): Result<Unit> = repository.deleteInvoice(invoiceId)
}
