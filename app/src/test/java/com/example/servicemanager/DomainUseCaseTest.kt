package com.example.servicemanager

import com.example.servicemanager.core.domain.AddSparePart
import com.example.servicemanager.core.domain.AddSparePartRequest
import com.example.servicemanager.core.domain.AddServiceOrderRequest
import com.example.servicemanager.core.domain.Customer
import com.example.servicemanager.core.domain.Device
import com.example.servicemanager.core.domain.DiagnosticAnswer
import com.example.servicemanager.core.domain.DiagnosticSession
import com.example.servicemanager.core.domain.DiagnosticsRepository
import com.example.servicemanager.core.domain.GetServiceBuckets
import com.example.servicemanager.core.domain.Issue
import com.example.servicemanager.core.domain.LifecycleBucket
import com.example.servicemanager.core.domain.PaymentSummary
import com.example.servicemanager.core.domain.PriorityLevel
import com.example.servicemanager.core.domain.RunDiagnostics
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
import com.example.servicemanager.core.domain.SyncMetadata
import com.example.servicemanager.core.domain.SyncState
import com.example.servicemanager.core.domain.TechnicianAssignment
import com.example.servicemanager.core.domain.TimelineState
import com.example.servicemanager.core.domain.UpdateServiceStatus
import com.example.servicemanager.core.domain.UpdateStatusRequest
import com.example.servicemanager.core.domain.WorkflowTimelineEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainUseCaseTest {
    @Test
    fun `run diagnostics rejects impossible qc totals`() = runTest {
        val useCase = RunDiagnostics(object : DiagnosticsRepository {
            override suspend fun runDiagnostics(serviceId: Long, request: RunDiagnosticsRequest): Result<Unit> = Result.success(Unit)
        })

        val result = useCase(
            serviceId = 1L,
            request = RunDiagnosticsRequest(
                primaryPowerRailConnection = DiagnosticAnswer.YES,
                dataLinkHandshake = DiagnosticAnswer.YES,
                internalSensorCalibration = DiagnosticAnswer.NO,
                qcTotal = 4,
                qcPassed = 3,
                qcFailed = 2,
            ),
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `completed risky job requires note`() = runTest {
        val useCase = UpdateServiceStatus(object : com.example.servicemanager.core.domain.StatusWorkflowRepository {
            override suspend fun updateStatus(serviceId: Long, request: UpdateStatusRequest): Result<Unit> = Result.success(Unit)
        })

        val result = useCase(
            serviceId = 1L,
            currentService = fakeService(),
            request = UpdateStatusRequest(ServiceStatus.COMPLETED, ""),
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `add spare part validates required fields`() = runTest {
        val useCase = AddSparePart(object : SparePartRepository {
            override suspend fun addSparePart(serviceId: Long, request: AddSparePartRequest): Result<Unit> = Result.success(Unit)
        })

        val result = useCase(
            serviceId = 1L,
            request = AddSparePartRequest("", "", "STATION-04-B", "AUTOMATIC"),
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `service buckets preserve ready jobs`() = runTest {
        val repository = object : ServiceOrderRepository {
            override fun observeServiceBuckets(query: String): Flow<ServiceBuckets> = flowOf(
                ServiceBuckets(
                    inProgress = emptyList(),
                    incoming = emptyList(),
                    readyForPickup = listOf(
                        ServiceSummary(
                            serviceId = 4L,
                            serviceCode = "SN-8755-Z",
                            customerName = "Lara Croft",
                            deviceLabel = "iPad Pro 12.9",
                            status = ServiceStatus.READY_FOR_PICKUP,
                            priority = PriorityLevel.MEDIUM,
                            bucket = LifecycleBucket.READY_FOR_PICKUP,
                            updatedAt = 1L,
                        ),
                    ),
                    completed = emptyList(),
                ),
            )

            override fun observeServiceDetail(serviceId: Long): Flow<ServiceOrder?> = flowOf(null)
            override suspend fun addServiceNote(serviceId: Long, note: String) = Unit
            override suspend fun addServiceOrder(request: AddServiceOrderRequest): Result<Long> = Result.success(1L)
            override suspend fun seedIfEmpty() = Unit
            override fun observeCustomerProfiles(): Flow<List<com.example.servicemanager.core.domain.CustomerProfile>> = flowOf(emptyList())
            override suspend fun addCustomerProfile(profile: com.example.servicemanager.core.domain.CustomerProfile) = Unit
            override suspend fun searchCustomers(query: String): List<Customer> = emptyList()
            override suspend fun searchBrands(query: String): List<com.example.servicemanager.core.domain.Brand> = emptyList()
            override fun observeBrands(): Flow<List<com.example.servicemanager.core.domain.Brand>> = flowOf(emptyList())
            override suspend fun addBrand(brand: com.example.servicemanager.core.domain.Brand) = Unit
            override suspend fun deleteBrand(brand: com.example.servicemanager.core.domain.Brand) = Unit
            override fun observeDeviceTypes(): Flow<List<com.example.servicemanager.core.domain.DeviceType>> = flowOf(emptyList())
            override suspend fun addDeviceType(deviceType: com.example.servicemanager.core.domain.DeviceType) = Unit
            override suspend fun deleteDeviceType(deviceType: com.example.servicemanager.core.domain.DeviceType) = Unit
            override fun observeStatusConfigs(): Flow<List<com.example.servicemanager.core.domain.ServiceStatusConfig>> = flowOf(emptyList())
            override suspend fun updateStatusConfig(config: com.example.servicemanager.core.domain.ServiceStatusConfig) = Unit
            override suspend fun setStatusConfigActive(status: ServiceStatus, isActive: Boolean) = Unit
            override fun observeQCChecklist(): Flow<List<com.example.servicemanager.core.domain.QCChecklistItem>> = flowOf(emptyList())
            override suspend fun addQCChecklistItem(item: com.example.servicemanager.core.domain.QCChecklistItem) = Unit
            override suspend fun deleteQCChecklistItem(item: com.example.servicemanager.core.domain.QCChecklistItem) = Unit
        }

        val buckets = GetServiceBuckets(repository)("").firstValue()
        assertEquals(1, buckets.readyForPickup.size)
    }

    private fun fakeService(): ServiceOrder =
        ServiceOrder(
            sync = SyncMetadata(1, "uuid", 1, SyncState.LOCAL_ONLY),
            serviceCode = "SN-8829-X",
            status = ServiceStatus.DIAGNOSTICS,
            priority = PriorityLevel.HIGH,
            intent = "Repair",
            reportedProblem = "Needs work",
            customer = Customer("Marcus Holloway", "123"),
            device = Device("Laptop", "MacBook Pro", "M2", "ABC"),
            issues = listOf(Issue(1, "Leak", "Replace")),
            spareParts = emptyList(),
            diagnostics = listOf(
                DiagnosticSession(
                    sync = SyncMetadata(1, "uuid", 1, SyncState.LOCAL_ONLY),
                    serviceId = 1,
                    primaryPowerRailConnection = DiagnosticAnswer.YES,
                    dataLinkHandshake = DiagnosticAnswer.NOT_TESTED,
                    internalSensorCalibration = DiagnosticAnswer.NO,
                    qcTotal = 12,
                    qcPassed = 10,
                    qcFailed = 2,
                    createdAt = 1,
                ),
            ),
            transitions = listOf(StatusTransition(1, 1, ServiceStatus.IN_PROGRESS, ServiceStatus.DIAGNOSTICS, "diag", 1)),
            timeline = listOf(WorkflowTimelineEntry(1, 1, "Initial Diagnostics", "Done", TimelineState.DONE)),
            notes = listOf(ServiceNote(SyncMetadata(2, "uuid2", 2, SyncState.LOCAL_ONLY), 1, "Note", 2)),
            assignment = TechnicianAssignment("Kailash", "Lead"),
            payment = PaymentSummary(1450.0, 500.0),
        )
}

private suspend fun <T> Flow<T>.firstValue(): T {
    var value: T? = null
    collect {
        value = it
        return@collect
    }
    return checkNotNull(value)
}
