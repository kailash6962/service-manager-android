package com.example.servicemanager.features

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.servicemanager.core.data.PreferencesStore
import com.example.servicemanager.core.data.ServiceSortMode
import com.example.servicemanager.core.data.CompanyProfile
import com.example.servicemanager.core.designsystem.DesignTokens
import com.example.servicemanager.core.designsystem.DetailBlock
import com.example.servicemanager.core.designsystem.ModalSurface
import com.example.servicemanager.core.designsystem.PrimaryActionButton
import com.example.servicemanager.core.designsystem.ScreenBackground
import com.example.servicemanager.core.designsystem.ScreenTitle
import com.example.servicemanager.core.designsystem.SecondaryActionButton
import com.example.servicemanager.core.designsystem.SectionLabel
import com.example.servicemanager.core.designsystem.SegmentedChoiceRow
import com.example.servicemanager.core.designsystem.SentinelCard
import com.example.servicemanager.core.designsystem.SentinelDropdownField
import com.example.servicemanager.core.designsystem.SentinelSnackbarHost
import com.example.servicemanager.core.designsystem.WarningPanel
import android.util.Log
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.servicemanager.core.designsystem.StatusChoiceList
import com.example.servicemanager.core.designsystem.SentinelTextField
import com.example.servicemanager.core.designsystem.SentinelTheme
import com.example.servicemanager.core.designsystem.StatusBadge
import com.example.servicemanager.core.designsystem.TimelineRow
import com.example.servicemanager.core.domain.AddServiceNote
import com.example.servicemanager.core.domain.AddServiceOrder
import com.example.servicemanager.core.domain.AddServiceOrderRequest
import com.example.servicemanager.core.domain.AddSparePart
import com.example.servicemanager.core.domain.AddSparePartRequest
import com.example.servicemanager.core.domain.DiagnosticAnswer
import com.example.servicemanager.core.domain.GetServiceBuckets
import com.example.servicemanager.core.domain.GetServiceDetail
import com.example.servicemanager.core.domain.PriorityLevel
import com.example.servicemanager.core.domain.RunDiagnostics
import com.example.servicemanager.core.domain.RunDiagnosticsRequest
import com.example.servicemanager.core.domain.ServiceBuckets
import com.example.servicemanager.core.domain.ServiceOrder
import com.example.servicemanager.core.domain.ServiceOrderRepository
import com.example.servicemanager.core.domain.ServiceStatus
import com.example.servicemanager.core.domain.ServiceSummary
import com.example.servicemanager.core.domain.StatusWorkflowRepository
import com.example.servicemanager.core.domain.UpdateServiceStatus
import com.example.servicemanager.core.domain.UpdateStatusRequest
import com.example.servicemanager.core.domain.GetInvoices
import com.example.servicemanager.core.domain.CreateInvoice
import com.example.servicemanager.core.domain.DeleteInvoice
import com.example.servicemanager.core.domain.Invoice
import com.example.servicemanager.core.domain.InvoiceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

object Routes {
    const val ServiceList = "service_list"
    const val Customers = "customers"
    const val ServiceDetail = "service_detail/{serviceId}"
    const val Diagnostics = "diagnostics/{serviceId}"
    const val StatusUpdate = "status_update/{serviceId}"
    const val AddSparePart = "add_spare_part/{serviceId}"
    const val AddService = "add_service"
    const val CreateCustomer = "create_customer"
    const val Settings = "settings"
    const val SettingsBrands = "settings_brands"
    const val SettingsDeviceTypes = "settings_device_types"
    const val SettingsQCChecklist = "settings_qc_checklist"
    const val SettingsStatusWorkflow = "settings_status_workflow"
    const val SettingsProfile = "settings_profile"
    const val Invoice = "invoice"

    fun serviceDetail(id: Long) = "service_detail/$id"
    fun diagnostics(id: Long) = "diagnostics/$id"
    fun statusUpdate(id: Long) = "status_update/$id"
    fun addSparePart(id: Long) = "add_spare_part/$id"
}

private const val RESULT_CUSTOMER_NAME = "result_customer_name"
private const val RESULT_CUSTOMER_PHONE = "result_customer_phone"
private const val RESULT_CUSTOMER_TYPE = "result_customer_type"

data class ServiceListUiState(
    val query: String = "",
    val sortMode: ServiceSortMode = ServiceSortMode.UPDATED_DESC,
    val buckets: ServiceBuckets = ServiceBuckets(emptyList(), emptyList(), emptyList(), emptyList()),
)

data class ServiceDetailUiState(
    val service: ServiceOrder? = null,
)

data class DiagnosticsUiState(
    val primaryPower: DiagnosticAnswer = DiagnosticAnswer.NOT_TESTED,
    val dataLink: DiagnosticAnswer = DiagnosticAnswer.NOT_TESTED,
    val sensorCalibration: DiagnosticAnswer = DiagnosticAnswer.NOT_TESTED,
    val qcAnswers: Map<Long, DiagnosticAnswer> = emptyMap(),
    val isSaving: Boolean = false,
)

data class StatusUpdateUiState(
    val selectedStatus: ServiceStatus = ServiceStatus.IN_PROGRESS,
    val note: String = "",
    val isSaving: Boolean = false,
)

data class AddSparePartUiState(
    val name: String = "",
    val quantity: String = "1",
    val isSaving: Boolean = false,
)

data class AddServiceUiState(
    val customerName: String = "",
    val customerPhone: String = "",
    val customerType: String = "Individual",
    val customerSuggestions: List<com.example.servicemanager.core.domain.Customer> = emptyList(),
    val deviceType: String = "",
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val serialNumber: String = "",
    val intent: String = "NEW",
    val reportedProblem: String = "",
    val status: ServiceStatus = ServiceStatus.QUEUED,
    val priority: PriorityLevel = PriorityLevel.LOW,
    val estimate: String = "",
    val advance: String = "",
    val capturedImages: List<Uri> = emptyList(),
    val isSaving: Boolean = false,
)

data class CreateCustomerUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val company: String = "",
    val address: String = "",
    val customerType: String = "Individual",
    val isSaving: Boolean = false,
)

data class CustomerDirectoryUiState(
    val customers: List<com.example.servicemanager.core.domain.CustomerProfile> = emptyList(),
)

data class SettingsUiState(
    val statusConfigs: List<com.example.servicemanager.core.domain.ServiceStatusConfig> = emptyList(),
    val statusOrder: List<ServiceStatus> = ServiceStatus.values().toList(),
    val brands: List<com.example.servicemanager.core.domain.Brand> = emptyList(),
    val deviceTypes: List<com.example.servicemanager.core.domain.DeviceType> = emptyList(),
    val qcChecklist: List<com.example.servicemanager.core.domain.QCChecklistItem> = emptyList(),
)

data class InvoiceUiState(
    val invoices: List<Invoice> = emptyList(),
)

data class ProfileSettingsUiState(
    val companyName: String = "Sentinel HUB",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val taxId: String = "",
    val otherDetails: String = "",
    val isSaving: Boolean = false,
)

private data class StatusConfigDraft(
    val minutes: String,
    val fixedTime: String,
    val showQcWarning: Boolean,
)

private fun normalizeStatusInput(input: String): String =
    input.trim()
        .uppercase()
        .replace(Regex("[^A-Z0-9]+"), "_")
        .trim('_')

private fun ServiceStatus.matchesStatusInput(input: String): Boolean =
    normalizeStatusInput(input) == name

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val getInvoices: GetInvoices,
    private val deleteInvoice: DeleteInvoice,
) : ViewModel() {
    val uiState = getInvoices().map { InvoiceUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvoiceUiState())

    fun removeInvoice(id: Long) {
        viewModelScope.launch {
            deleteInvoice(id)
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: com.example.servicemanager.core.domain.ServiceOrderRepository,
) : ViewModel() {
    val uiState = combine(
        repository.observeStatusConfigs(),
        repository.observeStatusOrder(),
        repository.observeBrands(),
        repository.observeDeviceTypes(),
        repository.observeQCChecklist()
    ) { status, order, brands, types, qc ->
        SettingsUiState(status, order, brands, types, qc)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateConfig(config: com.example.servicemanager.core.domain.ServiceStatusConfig) {
        viewModelScope.launch {
            repository.updateStatusConfig(config)
        }
    }

    fun setStatusActive(status: com.example.servicemanager.core.domain.ServiceStatus, isActive: Boolean) {
        viewModelScope.launch {
            repository.setStatusConfigActive(status, isActive)
        }
    }

    fun setStatusOrder(order: List<com.example.servicemanager.core.domain.ServiceStatus>) {
        viewModelScope.launch {
            repository.setStatusOrder(order)
        }
    }

    // Brands
    fun addBrand(name: String) {
        viewModelScope.launch { repository.addBrand(com.example.servicemanager.core.domain.Brand(name)) }
    }
    fun deleteBrand(brand: com.example.servicemanager.core.domain.Brand) {
        viewModelScope.launch { repository.deleteBrand(brand) }
    }

    // Device Types
    fun addDeviceType(name: String) {
        viewModelScope.launch { repository.addDeviceType(com.example.servicemanager.core.domain.DeviceType(name)) }
    }
    fun deleteDeviceType(type: com.example.servicemanager.core.domain.DeviceType) {
        viewModelScope.launch { repository.deleteDeviceType(type) }
    }

    // QC Checklist
    fun addQCChecklistItem(name: String) {
        viewModelScope.launch { repository.addQCChecklistItem(com.example.servicemanager.core.domain.QCChecklistItem(0, name)) }
    }
    fun deleteQCChecklistItem(item: com.example.servicemanager.core.domain.QCChecklistItem) {
        viewModelScope.launch { repository.deleteQCChecklistItem(item) }
    }
}

@HiltViewModel
class CompanyProfileViewModel @Inject constructor(
    private val preferencesStore: PreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState = _uiState.asStateFlow()

    val companyProfile = preferencesStore.companyProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompanyProfile())

    init {
        viewModelScope.launch {
            companyProfile.collectLatest { profile ->
                _uiState.update {
                    it.copy(
                        companyName = profile.companyName,
                        address = profile.address,
                        phone = profile.phone,
                        email = profile.email,
                        taxId = profile.taxId,
                        otherDetails = profile.otherDetails,
                    )
                }
            }
        }
    }

    fun onCompanyNameChanged(v: String) { _uiState.update { it.copy(companyName = v) } }
    fun onAddressChanged(v: String) { _uiState.update { it.copy(address = v) } }
    fun onPhoneChanged(v: String) { _uiState.update { it.copy(phone = v) } }
    fun onEmailChanged(v: String) { _uiState.update { it.copy(email = v) } }
    fun onTaxIdChanged(v: String) { _uiState.update { it.copy(taxId = v) } }
    fun onOtherDetailsChanged(v: String) { _uiState.update { it.copy(otherDetails = v) } }

    fun save() {
        viewModelScope.launch {
            if (_uiState.value.companyName.isBlank()) {
                return@launch
            }
            _uiState.update { it.copy(isSaving = true) }
            preferencesStore.updateCompanyProfile(
                CompanyProfile(
                    companyName = _uiState.value.companyName,
                    address = _uiState.value.address,
                    phone = _uiState.value.phone,
                    email = _uiState.value.email,
                    taxId = _uiState.value.taxId,
                    otherDetails = _uiState.value.otherDetails,
                )
            )
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}

@HiltViewModel
class CreateCustomerViewModel @Inject constructor(
    private val repository: ServiceOrderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateCustomerUiState())
    val uiState = _uiState.asStateFlow()
    private val eventFlow = MutableSharedFlow<UiEvent>()
    val events = eventFlow.asSharedFlow()

    fun onNameChanged(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onPhoneChanged(v: String) { _uiState.value = _uiState.value.copy(phone = v) }
    fun onEmailChanged(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onCompanyChanged(v: String) { _uiState.value = _uiState.value.copy(company = v) }
    fun onAddressChanged(v: String) { _uiState.value = _uiState.value.copy(address = v) }
    fun onTypeChanged(v: String) { _uiState.value = _uiState.value.copy(customerType = v) }

    fun submit() {
        if (_uiState.value.name.isBlank()) {
            viewModelScope.launch {
                eventFlow.emit(UiEvent.Message("Please enter customer name"))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.addCustomerProfile(
                com.example.servicemanager.core.domain.CustomerProfile(
                    name = _uiState.value.name,
                    phone = _uiState.value.phone,
                    email = _uiState.value.email,
                    company = _uiState.value.company,
                    address = _uiState.value.address,
                    type = _uiState.value.customerType,
                )
            )
            _uiState.update { it.copy(isSaving = false) }
            eventFlow.emit(UiEvent.Success)
        }
    }
}

@HiltViewModel
class CustomerDirectoryViewModel @Inject constructor(
    repository: ServiceOrderRepository,
) : ViewModel() {
    val uiState = repository.observeCustomerProfiles()
        .map { CustomerDirectoryUiState(customers = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomerDirectoryUiState())
}

sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    object Success : UiEvent
}

@HiltViewModel
class ServiceListViewModel @Inject constructor(
    private val getServiceBuckets: GetServiceBuckets,
    private val preferencesStore: PreferencesStore,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val uiState: StateFlow<ServiceListUiState> = combine(
        _query.debounce(300),
        preferencesStore.sortMode,
    ) { q, sort -> q to sort }
        .flatMapLatest { (q, sort) ->
            getServiceBuckets(q).map { buckets ->
                ServiceListUiState(query = q, sortMode = sort, buckets = buckets)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ServiceListUiState())

    fun onQueryChanged(q: String) {
        _query.value = q
    }

    fun setSortMode(mode: ServiceSortMode) {
        viewModelScope.launch {
            preferencesStore.setSortMode(mode)
        }
    }
}

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getServiceDetail: GetServiceDetail,
    private val addServiceNote: AddServiceNote,
    private val createInvoice: CreateInvoice,
) : ViewModel() {
    private val serviceId: Long = checkNotNull(savedStateHandle["serviceId"])
    val uiState: StateFlow<ServiceDetailUiState> = getServiceDetail(serviceId)
        .map { ServiceDetailUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ServiceDetailUiState())

    private val eventFlow = MutableSharedFlow<UiEvent>()
    val events = eventFlow.asSharedFlow()

    fun addQuickNote() {
        viewModelScope.launch {
            addServiceNote(serviceId, "Quick follow-up required.")
                .onSuccess { eventFlow.emit(UiEvent.Message("Note added.")) }
                .onFailure { eventFlow.emit(UiEvent.Message(it.message ?: "Error")) }
        }
    }

    fun generateInvoice() {
        viewModelScope.launch {
            createInvoice(serviceId)
                .onSuccess { eventFlow.emit(UiEvent.Message("Invoice generated.")) }
                .onFailure { eventFlow.emit(UiEvent.Message(it.message ?: "Error")) }
        }
    }

}

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getServiceDetail: GetServiceDetail,
    private val runDiagnostics: RunDiagnostics,
    private val repository: ServiceOrderRepository,
) : ViewModel() {
    private val serviceId: Long = checkNotNull(savedStateHandle["serviceId"])
    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState = _uiState.asStateFlow()

    val service = getServiceDetail(serviceId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val qcChecklist = repository.observeQCChecklist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val eventFlow = MutableSharedFlow<UiEvent>()
    val events = eventFlow.asSharedFlow()

    fun onPowerChanged(v: DiagnosticAnswer) { _uiState.update { it.copy(primaryPower = v) } }
    fun onDataChanged(v: DiagnosticAnswer) { _uiState.update { it.copy(dataLink = v) } }
    fun onCalibrationChanged(v: DiagnosticAnswer) { _uiState.update { it.copy(sensorCalibration = v) } }

    fun onQCAnswerChanged(itemId: Long, v: DiagnosticAnswer) {
        _uiState.update { state ->
            val next = state.qcAnswers.toMutableMap()
            next[itemId] = v
            state.copy(qcAnswers = next)
        }
    }

    fun submit() {
        viewModelScope.launch {
            val checklist = qcChecklist.value
            val answers = _uiState.value.qcAnswers
            val total = checklist.size
            val passed = checklist.count { answers[it.id] == DiagnosticAnswer.YES }
            val failed = checklist.count { answers[it.id] == DiagnosticAnswer.NO }

            _uiState.update { it.copy(isSaving = true) }
            runDiagnostics(
                serviceId,
                RunDiagnosticsRequest(
                    primaryPowerRailConnection = _uiState.value.primaryPower,
                    dataLinkHandshake = _uiState.value.dataLink,
                    internalSensorCalibration = _uiState.value.sensorCalibration,
                    qcTotal = total,
                    qcPassed = passed,
                    qcFailed = failed,
                ),
            ).onSuccess {
                eventFlow.emit(UiEvent.Success)
            }.onFailure {
                eventFlow.emit(UiEvent.Message(it.message ?: "Error saving diagnostics"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}

@HiltViewModel
class StatusUpdateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getServiceDetail: GetServiceDetail,
    private val updateServiceStatus: UpdateServiceStatus,
    private val repository: ServiceOrderRepository,
) : ViewModel() {
    private val serviceId: Long = checkNotNull(savedStateHandle["serviceId"])
    private val _uiState = MutableStateFlow(StatusUpdateUiState())
    val uiState = _uiState.asStateFlow()

    private var hasInitializedSelectedStatus = false

    val service = getServiceDetail(serviceId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val statusConfigs = repository.observeStatusConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val statusOrder = repository.observeStatusOrder()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ServiceStatus.values().toList())
    val qcChecklist = repository.observeQCChecklist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val eventFlow = MutableSharedFlow<UiEvent>()
    val events = eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            service.collect { currentService ->
                if (!hasInitializedSelectedStatus && currentService != null) {
                    hasInitializedSelectedStatus = true
                    _uiState.update { it.copy(selectedStatus = currentService.status) }
                }
            }
        }
    }

    fun onStatusChanged(v: ServiceStatus) { _uiState.update { it.copy(selectedStatus = v) } }
    fun onNoteChanged(v: String) { _uiState.update { it.copy(note = v) } }

    fun submit() {
        val currentService = service.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            updateServiceStatus(
                serviceId,
                currentService,
                UpdateStatusRequest(_uiState.value.selectedStatus, _uiState.value.note),
            ).onSuccess {
                eventFlow.emit(UiEvent.Success)
            }.onFailure {
                eventFlow.emit(UiEvent.Message(it.message ?: "Error updating status"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}

@HiltViewModel
class AddSparePartViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addSparePart: AddSparePart,
    private val getServiceDetail: GetServiceDetail,
) : ViewModel() {
    private val serviceId: Long = checkNotNull(savedStateHandle["serviceId"])
    private val _uiState = MutableStateFlow(AddSparePartUiState())
    val uiState = _uiState.asStateFlow()
    private var namePrefill = ""
    private var didApplyInitialPrefill = false

    private val eventFlow = MutableSharedFlow<UiEvent>()
    val events = eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            getServiceDetail(serviceId).collect { service ->
                val prefill = listOf(service?.device?.brand, service?.device?.model)
                    .filterNotNull()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                if (prefill.isNotBlank()) {
                    namePrefill = prefill
                    if (!didApplyInitialPrefill && _uiState.value.name.isBlank()) {
                        didApplyInitialPrefill = true
                        _uiState.update { it.copy(name = namePrefill) }
                    }
                }
            }
        }
    }

    fun onNameChanged(v: String) { _uiState.update { it.copy(name = v) } }
    fun onQuantityChanged(v: String) {
        _uiState.update { it.copy(quantity = v.filter { ch -> ch.isDigit() }.ifBlank { "1" }) }
    }

    fun submit(saveAndAdd: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val qty = _uiState.value.quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1
            addSparePart(
                serviceId,
                AddSparePartRequest(
                    name = _uiState.value.name.trim(),
                    storageBoxNumber = "N/A",
                    assignedStation = "N/A",
                    inventoryLevel = qty.toString(),
                ),
            ).onSuccess {
                if (saveAndAdd) {
                    _uiState.update { it.copy(name = namePrefill.ifBlank { "" }, quantity = "1") }
                    eventFlow.emit(UiEvent.Message("Spare requirement saved."))
                } else {
                    eventFlow.emit(UiEvent.Success)
                }
            }.onFailure {
                eventFlow.emit(UiEvent.Message(it.message ?: "Error logging spare part"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val addServiceOrder: AddServiceOrder,
    private val repository: ServiceOrderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddServiceUiState())
    val uiState = _uiState.asStateFlow()

    private val eventFlow = MutableSharedFlow<UiEvent>()
    val events = eventFlow.asSharedFlow()

    val deviceTypes = repository.observeDeviceTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brands = repository.observeBrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _uiState.map { it.customerName }
                .distinctUntilChanged()
                .debounce(300)
                .collectLatest { query ->
                    if (query.length >= 2) {
                        val suggestions = repository.searchCustomers(query)
                        _uiState.update { it.copy(customerSuggestions = suggestions) }
                    } else {
                        _uiState.update { it.copy(customerSuggestions = emptyList()) }
                    }
                }
        }
    }

    fun onCustomerNameChanged(v: String) { _uiState.update { it.copy(customerName = v) } }
    fun onCustomerPhoneChanged(v: String) { _uiState.update { it.copy(customerPhone = v) } }
    fun onCustomerTypeChanged(v: String) { _uiState.update { it.copy(customerType = v) } }
    fun onDeviceTypeChanged(v: String) { _uiState.update { it.copy(deviceType = v) } }
    fun onBrandChanged(v: String) { _uiState.update { it.copy(deviceBrand = v) } }
    fun onModelChanged(v: String) { _uiState.update { it.copy(deviceModel = v) } }
    fun onSerialChanged(v: String) { _uiState.update { it.copy(serialNumber = v) } }
    fun onIntentChanged(v: String) { _uiState.update { it.copy(intent = v) } }
    fun onProblemChanged(v: String) { _uiState.update { it.copy(reportedProblem = v) } }
    fun onStatusChanged(v: ServiceStatus) { _uiState.update { it.copy(status = v) } }
    fun onPriorityChanged(v: PriorityLevel) { _uiState.update { it.copy(priority = v) } }
    fun onEstimateChanged(v: String) { _uiState.update { it.copy(estimate = v) } }
    fun onAdvanceChanged(v: String) { _uiState.update { it.copy(advance = v) } }

    fun onPhotosCaptured(uris: List<Uri>) {
        _uiState.update { it.copy(capturedImages = it.capturedImages + uris) }
    }

    fun onPhotoCaptured(uri: Uri?) {
        uri?.let { u ->
            _uiState.update { it.copy(capturedImages = it.capturedImages + u) }
        }
    }

    fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            addServiceOrder(
                AddServiceOrderRequest(
                    customerName = _uiState.value.customerName,
                    customerPhone = _uiState.value.customerPhone,
                    customerType = _uiState.value.customerType,
                    deviceType = _uiState.value.deviceType,
                    deviceBrand = _uiState.value.deviceBrand,
                    deviceModel = _uiState.value.deviceModel,
                    serialNumber = _uiState.value.serialNumber,
                    intent = _uiState.value.intent,
                    reportedProblem = _uiState.value.reportedProblem,
                    status = _uiState.value.status,
                    priority = _uiState.value.priority,
                    initialEstimate = _uiState.value.estimate.toDoubleOrNull() ?: 0.0,
                    advancePaid = _uiState.value.advance.toDoubleOrNull() ?: 0.0,
                ),
            ).onSuccess {
                eventFlow.emit(UiEvent.Success)
            }.onFailure {
                eventFlow.emit(UiEvent.Message(it.message ?: "Error creating service order"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}

@Composable
fun ServiceManagerApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val profileViewModel: CompanyProfileViewModel = hiltViewModel()
    val companyProfile by profileViewModel.companyProfile.collectAsStateWithLifecycle()

    SentinelTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        companyProfile.companyName,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        label = { Text("Service Orders") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.ServiceList)
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Call, contentDescription = null) },
                        label = { Text("Customers") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.Customers)
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                        label = { Text("Invoices") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.Invoice)
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.Settings)
                        }
                    )
                }
            }
        ) {
            NavHost(navController = navController, startDestination = Routes.ServiceList) {
                composable(Routes.ServiceList) {
                    ServiceListRoute(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        companyName = companyProfile.companyName,
                    )
                }
                composable(Routes.Customers) { CustomersRoute(navController) }
                composable(
                    Routes.ServiceDetail,
                    arguments = listOf(navArgument("serviceId") { type = NavType.LongType }),
                ) { ServiceDetailRoute(navController) }
                composable(
                    Routes.Diagnostics,
                    arguments = listOf(navArgument("serviceId") { type = NavType.LongType }),
                ) { DiagnosticsRoute(navController) }
                composable(
                    Routes.StatusUpdate,
                    arguments = listOf(navArgument("serviceId") { type = NavType.LongType }),
                ) { StatusUpdateRoute(navController) }
                composable(
                    Routes.AddSparePart,
                    arguments = listOf(navArgument("serviceId") { type = NavType.LongType }),
                ) { AddSparePartRoute(navController) }
                composable(Routes.AddService) { AddServiceRoute(navController) }
                composable(Routes.CreateCustomer) { CreateCustomerRoute(navController) }
                composable(Routes.Settings) { SettingsRoute(navController) }
                composable(Routes.SettingsProfile) { SettingsProfileRoute(navController) }
                composable(Routes.SettingsBrands) { SettingsBrandsRoute(navController) }
                composable(Routes.SettingsDeviceTypes) { SettingsDeviceTypesRoute(navController) }
                composable(Routes.SettingsQCChecklist) { SettingsQCChecklistRoute(navController) }
                composable(Routes.SettingsStatusWorkflow) { SettingsStatusWorkflowRoute(navController) }
                composable(Routes.Invoice) { InvoiceRoute(navController) }
            }
        }
    }
}

@Composable
private fun CustomersRoute(
    navController: NavHostController,
    viewModel: CustomerDirectoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DesignTokens.Ink)
                        }
                        Text(
                            "Customers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DesignTokens.Ink,
                        )
                    }
                }
            },
            floatingActionButton = {
                Surface(
                    onClick = { navController.navigate(Routes.CreateCustomer) },
                    shape = RoundedCornerShape(4.dp),
                    color = DesignTokens.Ink,
                    modifier = Modifier.size(56.dp),
                    shadowElevation = 8.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    }
                }
            },
        ) { padding ->
            if (uiState.customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No customers found. Add your first customer.", style = MaterialTheme.typography.bodyLarge, color = DesignTokens.MutedInk)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.customers) { customer ->
                        SentinelCard {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (customer.phone.isNotBlank()) {
                                    Text(customer.phone, style = MaterialTheme.typography.bodyMedium, color = DesignTokens.MutedInk)
                                }
                                if (customer.company.isNotBlank()) {
                                    Text(customer.company, style = MaterialTheme.typography.bodyMedium, color = DesignTokens.MutedInk)
                                }
                                if (customer.address.isNotBlank()) {
                                    Text(customer.address, style = MaterialTheme.typography.bodySmall, color = DesignTokens.MutedInk)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceListRoute(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    companyName: String,
    viewModel: ServiceListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 8.dp, bottom = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onOpenDrawer) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = DesignTokens.Ink)
                                }
                                Text(
                                    companyName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DesignTokens.Ink,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = DesignTokens.Ink, modifier = Modifier.size(20.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(DesignTokens.Ink),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "K",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            TextField(
                                value = query,
                                onValueChange = viewModel::onQueryChanged,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                placeholder = { Text("Search orders, customers...", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DesignTokens.SurfaceCard,
                                    unfocusedContainerColor = DesignTokens.SurfaceCard,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                shape = RoundedCornerShape(4.dp),
                                singleLine = true,
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(DesignTokens.SurfaceCard)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                    .clickable { /* TODO */ },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                Surface(
                    onClick = { navController.navigate(Routes.AddService) },
                    shape = RoundedCornerShape(4.dp),
                    color = DesignTokens.Ink,
                    modifier = Modifier.size(56.dp),
                    shadowElevation = 8.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    }
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                bucketSection("Active Lifecycle", uiState.buckets.inProgress, navController)
                bucketSection("Incoming Queue", uiState.buckets.incoming, navController)
                bucketSection("Ready for Handoff", uiState.buckets.readyForPickup, navController)
                bucketSection("Archive", uiState.buckets.completed, navController)
            }
        }
    }
}

private fun LazyListScope.bucketSection(
    label: String,
    items: List<ServiceSummary>,
    navController: NavHostController,
) {
    if (items.isEmpty()) return
    item {
        SectionLabel(label)
    }
    items(items) { summary ->
        ServiceSummaryCard(summary) {
            navController.navigate(Routes.serviceDetail(summary.serviceId))
        }
    }
}

@Composable
private fun ServiceSummaryCard(summary: ServiceSummary, onClick: () -> Unit) {
    SentinelCard(modifier = Modifier.clickable { onClick() }) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "#${summary.serviceCode}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DesignTokens.Ink,
                )
                StatusBadge(summary.status)
            }

            Column {
                Text(
                    summary.customerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    summary.deviceLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignTokens.MutedInk,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val icon = when (summary.deviceType.lowercase()) {
                        "phone" -> Icons.Default.TabletAndroid
                        "laptop" -> Icons.Default.LaptopMac
                        "watch" -> Icons.Default.Watch
                        else -> Icons.Default.Router
                    }
                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = DesignTokens.MutedInk)
                    Text(
                        summary.deviceType.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DesignTokens.MutedInk,
                    )
                }
                Text(
                    formatRelativeAge(summary.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = DesignTokens.MutedInk,
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp)).uppercase()
}

private fun formatDateTime(timestamp: Long?): String {
    if (timestamp == null) return "NOT ESTIMATED"
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp)).uppercase()
}

private fun formatRelativeAge(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diffMinutes = ((now - timestamp).coerceAtLeast(0L)) / 60_000L
    return when {
        diffMinutes < 1L -> "1 min"
        diffMinutes < 60L -> "$diffMinutes min"
        diffMinutes < 1_440L -> {
            val hours = diffMinutes / 60L
            if (hours == 1L) "1 hour" else "$hours hours"
        }
        else -> {
            val days = diffMinutes / 1_440L
            if (days == 1L) "1 day" else "$days days"
        }
    }
}

private fun formatInr(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
}

@Composable
private fun ServiceDetailRoute(
    navController: NavHostController,
    viewModel: ServiceDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is UiEvent.Message) snackbar.showSnackbar(event.text)
        }
    }
    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
	            bottomBar = {
	                val service = uiState.service
	                if (service != null) {
	                    Surface(
	                        color = Color.White,
	                        modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
	                    ) {
	                        Row(
	                            modifier = Modifier
	                                .fillMaxWidth()
	                                .padding(16.dp),
	                            horizontalArrangement = Arrangement.spacedBy(12.dp),
	                        ) {
	                            PrimaryActionButton(
	                                text = "UPDATE STATUS",
	                                modifier = Modifier.fillMaxWidth(),
	                                onClick = { navController.navigate(Routes.statusUpdate(service.sync.localId)) },
	                            )
	                        }
	                    }
	                }
	            },
            topBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DesignTokens.Ink)
                            }
                            Text(
                                "Service Detail",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = DesignTokens.Ink,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(32.dp)
                                .background(DesignTokens.Ink),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "K",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            val service = uiState.service
            if (service == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // Context Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DesignTokens.SurfaceCard)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "SERVICE ID",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                color = DesignTokens.MutedInk,
                            )
                            Text(
                                "#${service.serviceCode}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "STATUS",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                color = DesignTokens.MutedInk,
                            )
                            StatusBadge(service.status)
                        }
                    }

                    // ETA Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DesignTokens.SurfaceCard.copy(alpha = 0.5f))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = DesignTokens.Ink, modifier = Modifier.size(24.dp))
                        Column {
                            Text(
                                "EXPECTED COMPLETION",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                color = DesignTokens.MutedInk,
                            )
                            Text(
                                formatDateTime(service.expectedCompletionTime),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = DesignTokens.Ink
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        DetailSection("01. Customer Entity") {
                            SentinelCard {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        DetailBlock("Customer Name", service.customer.name, Modifier.weight(1f))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                                    .clickable { /* TODO */ },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, tint = DesignTokens.Ink, modifier = Modifier.size(20.dp))
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                                    .clickable { /* TODO */ },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(Icons.Default.ChatBubble, contentDescription = null, tint = DesignTokens.Ink, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        DetailBlock("Phone Number", service.customer.phone, Modifier.weight(1f))
                                        DetailBlock("Customer Type", service.customer.type, Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        DetailSection("02. Hardware Specification") {
                            Column(
                                modifier = Modifier
                                    .background(DesignTokens.SurfaceCard)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                            .padding(12.dp),
                                    ) {
                                        DetailBlock("Device Type", service.device.type)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                            .padding(12.dp),
                                    ) {
                                        DetailBlock("Brand", service.device.brand)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                            .padding(12.dp),
                                    ) {
                                        DetailBlock("Model", service.device.model)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                            .padding(12.dp),
                                    ) {
                                        DetailBlock("Serial Number", service.device.serialNumber)
                                    }
                                }
                            }
                        }

                        DetailSection("03. Issue Description") {
                            SentinelCard {
                                DetailBlock("Reported Problem", service.reportedProblem)
                            }
                        }

                        DetailSection(
                            "04. Issues & Spare Requirements",
                            action = {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .clickable { navController.navigate(Routes.addSparePart(service.sync.localId)) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add spare requirement",
                                        tint = DesignTokens.Ink,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            },
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                service.issues.forEach { issue ->
                                    SentinelCard {
                                        Text(issue.title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp))
                                        Text(
                                            "REQUIRES: ${issue.requirement.uppercase()}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                            color = DesignTokens.MutedInk,
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .background(DesignTokens.SurfaceCard)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                                .padding(12.dp),
                                        ) {
                                            Text(
                                                "SPARE NAME",
                                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.2.sp),
                                                color = DesignTokens.MutedInk,
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(0.35f)
                                                .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                                .padding(12.dp),
                                        ) {
                                            Text(
                                                "QTY",
                                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.2.sp),
                                                color = DesignTokens.MutedInk,
                                            )
                                        }
                                    }

                                    if (service.spareParts.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                                .padding(12.dp),
                                        ) {
                                            Text(
                                                "No spare requirements added.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DesignTokens.MutedInk,
                                            )
                                        }
                                    } else {
                                        service.spareParts.forEach { part ->
                                            val qty = part.inventoryLevel.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                                        .padding(12.dp),
                                                ) {
                                                    Text(part.name, style = MaterialTheme.typography.bodyLarge)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .weight(0.35f)
                                                        .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                                        .padding(12.dp),
                                                ) {
                                                    Text("$qty", style = MaterialTheme.typography.bodyLarge)
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { navController.navigate(Routes.addSparePart(service.sync.localId)) }
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = DesignTokens.Ink,
                                            )
                                            Text(
                                                "ADD SPARE REQUIREMENT",
                                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.4.sp),
                                                color = DesignTokens.Ink,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        DetailSection("05. Device Diagnostics") {
                            val latestDiagnostics = service.diagnostics.maxByOrNull { it.createdAt }
                            Column(
                                modifier = Modifier
                                    .background(DesignTokens.SurfaceCard)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                            .padding(12.dp),
                                    ) {
                                        DetailBlock("QC Total Count", "${latestDiagnostics?.qcTotal ?: 0}")
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                            .padding(12.dp),
                                    ) {
                                        DetailBlock("QC Passed Count", "${latestDiagnostics?.qcPassed ?: 0}")
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate(Routes.diagnostics(service.sync.localId)) }
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Build,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = DesignTokens.Ink,
                                        )
                                        Text(
                                            "RUN NEW DIAGNOSTIC",
                                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.4.sp),
                                            color = DesignTokens.Ink,
                                        )
                                    }
                                }
                            }
                        }

	                        DetailSection("06. Spare Parts Storage") {
	                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
	                                service.spareParts.forEach { part ->
	                                    SentinelCard {
	                                        DetailBlock(part.name, part.storageBoxNumber)
                                    }
                                }
                            }
                        }

                        DetailSection("07. Service Logistics") {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                SentinelCard {
                                    DetailBlock("Intent", service.intent)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "PRIORITY LEVEL",
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                        color = DesignTokens.MutedInk,
                                    )
                                    SegmentedChoiceRow(
                                        options = listOf("LOW", "MEDIUM", "HIGH", "URGENT"),
                                        selected = service.priority.name,
                                        onSelect = { /* TODO */ },
                                    )
                                }
                            }
                        }

                        DetailSection("08. Workflow Timeline") {
                            SentinelCard {
                                service.timeline.forEachIndexed { index, entry ->
                                    TimelineRow(
                                        title = entry.title,
                                        time = entry.subtitle,
                                        state = entry.state,
                                        isLast = index == service.timeline.lastIndex,
                                    )
                                }
                            }
                        }

                        DetailSection("09. Resource Allocation") {
                            Column(
                                modifier = Modifier
                                    .background(DesignTokens.SurfaceCard)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .padding(12.dp),
                                ) {
                                    DetailBlock("Assigned Technician", service.assignment.technicianName)
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                            .padding(12.dp),
                                    ) {
                                        DetailBlock("Initial Estimate", formatInr(service.payment.initialEstimate))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                                            .padding(12.dp),
                                    ) {
                                        DetailBlock("Advance Paid", formatInr(service.payment.advancePaid))
                                    }
                                }
                            }
                        }

                        DetailSection("10. Notes") {
                            SentinelCard {
                                DetailBlock("Internal Service Notes", service.notes.lastOrNull()?.body ?: "No notes available.")
                            }
                        }

                        DetailSection("11. Financial Documents") {
                            if (service.invoices.isEmpty()) {
                                Text("No invoices generated yet.", style = MaterialTheme.typography.bodyMedium, color = DesignTokens.MutedInk)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    service.invoices.forEach { invoice ->
                                        SentinelCard {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column {
                                                    Text("INVOICE #${invoice.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                    Text(formatTimestamp(invoice.createdAt), style = MaterialTheme.typography.labelSmall, color = DesignTokens.MutedInk)
                                                }
                                                Text(formatInr(invoice.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    label: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SectionLabel(label)
            }
            action?.invoke()
        }
        content()
    }
}

@Composable
private fun DiagnosticsRoute(
    navController: NavHostController,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val service by viewModel.service.collectAsStateWithLifecycle()
    val qcChecklist by viewModel.qcChecklist.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Success -> navController.popBackStack()
                is UiEvent.Message -> snackbar.showSnackbar(event.text)
            }
        }
    }

    ModalSurface(
        title = "Run Hardware Diagnostics",
        subtitle = "Verify hardware integrity for #${service?.serviceCode ?: "..."}",
        onDismiss = { navController.popBackStack() },
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("QC Checklist (From Settings)")
                if (qcChecklist.isEmpty()) {
                    Text(
                        "No QC checklist items found. Add items from Settings > QC Checklist.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DesignTokens.MutedInk,
                    )
                } else {
                    qcChecklist.forEach { item ->
                        val selected = uiState.qcAnswers[item.id] ?: DiagnosticAnswer.NOT_TESTED
                        SentinelCard {
                            Text(item.title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp))
                            if (!item.description.isNullOrBlank()) {
                                Text(item.description, style = MaterialTheme.typography.bodyMedium, color = DesignTokens.MutedInk)
                            }
                            SegmentedChoiceRow(
                                options = listOf("YES", "NO", "NOT_TESTED"),
                                selected = selected.name,
                                onSelect = { viewModel.onQCAnswerChanged(item.id, DiagnosticAnswer.valueOf(it)) },
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("QC Summary")
                val total = qcChecklist.size
                val passed = qcChecklist.count { uiState.qcAnswers[it.id] == DiagnosticAnswer.YES }
                val failed = qcChecklist.count { uiState.qcAnswers[it.id] == DiagnosticAnswer.NO }
                val notTested = total - passed - failed
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(12.dp),
                    ) { DetailBlock("Total", "$total") }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(12.dp),
                    ) { DetailBlock("Passed", "$passed") }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(12.dp),
                    ) { DetailBlock("Failed", "$failed") }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(12.dp),
                    ) { DetailBlock("Not Tested", "$notTested") }
                }
            }

            PrimaryActionButton(
                text = if (uiState.isSaving) "SAVING..." else "COMMIT DIAGNOSTICS",
                onClick = viewModel::submit,
                enabled = !uiState.isSaving,
            )
        }
    }
}

@Composable
private fun StatusUpdateRoute(
    navController: NavHostController,
    viewModel: StatusUpdateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val service by viewModel.service.collectAsStateWithLifecycle()
    val statusConfigs by viewModel.statusConfigs.collectAsStateWithLifecycle()
    val statusOrder by viewModel.statusOrder.collectAsStateWithLifecycle()
    val qcChecklist by viewModel.qcChecklist.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val configuredActive = statusConfigs.filter { it.isActive }.map { it.status }
    val activeStatuses = statusOrder
        .filter { it in configuredActive }
        .ifEmpty { ServiceStatus.values().toList() }
    val latestDiagnostics = service?.diagnostics?.maxByOrNull { it.createdAt }
    val selectedStatusConfig = statusConfigs.firstOrNull { it.status == uiState.selectedStatus }
    val effectiveQcTotalCount = latestDiagnostics?.qcTotal?.takeIf { it > 0 } ?: qcChecklist.size
    val qcPassedCount = latestDiagnostics?.qcPassed ?: 0
    val qcFailedCount = latestDiagnostics?.qcFailed ?: 0
    val qcNotPassedCount = (effectiveQcTotalCount - qcPassedCount).coerceAtLeast(0)
    val showQcWarning = (selectedStatusConfig?.showQcWarningForIncomplete == true) &&
        (qcFailedCount > 0 || qcNotPassedCount > 0)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Success -> navController.popBackStack()
                is UiEvent.Message -> snackbar.showSnackbar(event.text)
            }
        }
    }
    LaunchedEffect(activeStatuses) {
        if (activeStatuses.isNotEmpty() && uiState.selectedStatus !in activeStatuses) {
            viewModel.onStatusChanged(activeStatuses.first())
        }
    }

    ModalSurface(
        title = "Update Service Status",
        subtitle = "Transitioning #${service?.serviceCode ?: "..."}",
        onDismiss = { navController.popBackStack() },
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            StatusChoiceList(
                selectedStatus = uiState.selectedStatus,
                onStatusSelected = viewModel::onStatusChanged,
                options = activeStatuses,
            )

            SentinelTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChanged,
                label = "Service Note (Optional)",
            )

            if (showQcWarning) {
                WarningPanel(
                    title = "QC Incomplete",
                    message = "Passed $qcPassedCount of $effectiveQcTotalCount, Failed $qcFailedCount. This is only informational; you can still complete the status update.",
                )
            }

            PrimaryActionButton(
                text = if (uiState.isSaving) "UPDATING..." else "UPDATE STATUS",
                onClick = viewModel::submit,
                enabled = !uiState.isSaving,
            )
        }
    }
}

@Composable
private fun AddSparePartRoute(
    navController: NavHostController,
    viewModel: AddSparePartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Success -> navController.popBackStack()
                is UiEvent.Message -> snackbar.showSnackbar(event.text)
            }
        }
    }

    ModalSurface(
        title = "Add Spare Requirement",
        subtitle = "Log spare name and quantity",
        onDismiss = { navController.popBackStack() },
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SentinelTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = "Spare Name",
            )
            SentinelTextField(
                value = uiState.quantity,
                onValueChange = viewModel::onQuantityChanged,
                label = "Quantity",
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryActionButton(
                    text = if (uiState.isSaving) "SAVING..." else "SAVE & ADD",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.submit(saveAndAdd = true) },
                    enabled = !uiState.isSaving,
                )
                PrimaryActionButton(
                    text = if (uiState.isSaving) "SAVING..." else "SAVE",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.submit(saveAndAdd = false) },
                    enabled = !uiState.isSaving,
                )
            }
        }
    }
}

@Composable
private fun AddServiceRoute(
    navController: NavHostController,
    viewModel: AddServiceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deviceTypes by viewModel.deviceTypes.collectAsStateWithLifecycle()
    val brands by viewModel.brands.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.onPhotosCaptured(uris) },
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success) { /* Camera logic */ } },
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Success -> navController.popBackStack()
                is UiEvent.Message -> snackbar.showSnackbar(event.text)
            }
        }
    }
    LaunchedEffect(Unit) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        savedStateHandle.getStateFlow(RESULT_CUSTOMER_NAME, "").collectLatest { customerName ->
            if (customerName.isNotBlank()) {
                val customerPhone = savedStateHandle.get<String>(RESULT_CUSTOMER_PHONE).orEmpty()
                val customerType = savedStateHandle.get<String>(RESULT_CUSTOMER_TYPE).orEmpty().ifBlank { "Individual" }
                viewModel.onCustomerNameChanged(customerName)
                viewModel.onCustomerPhoneChanged(customerPhone)
                viewModel.onCustomerTypeChanged(customerType)
                savedStateHandle.remove<String>(RESULT_CUSTOMER_NAME)
                savedStateHandle.remove<String>(RESULT_CUSTOMER_PHONE)
                savedStateHandle.remove<String>(RESULT_CUSTOMER_TYPE)
            }
        }
    }

    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    PrimaryActionButton(
                        text = if (uiState.isSaving) "CREATING SERVICE..." else "CREATE SERVICE",
                        onClick = viewModel::submit,
                        enabled = !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                    )
                }
            },
            topBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DesignTokens.Ink)
                            }
                            Text(
                                "Create Service Order",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = DesignTokens.Ink,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DetailSection(
                    label = "01. Customer Entity",
                    action = {
                        Row(
                            modifier = Modifier.clickable { navController.navigate(Routes.CreateCustomer) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = DesignTokens.Ink, modifier = Modifier.size(18.dp))
                            Text("NEW CUSTOMER", style = MaterialTheme.typography.labelLarge, color = DesignTokens.Ink, fontSize = 10.sp)
                        }
                    },
                ) {
                    SentinelCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box {
                                SentinelTextField(
                                    value = uiState.customerName,
                                    onValueChange = viewModel::onCustomerNameChanged,
                                    label = "Customer Name",
                                )

                            if (uiState.customerSuggestions.isNotEmpty() && uiState.customerName.isNotBlank() && uiState.customerName != uiState.customerSuggestions.firstOrNull()?.name) {
                                Surface(
                                    modifier = Modifier
                                        .padding(top = 60.dp)
                                        .fillMaxWidth()
                                        .zIndex(10f),
                                    tonalElevation = 8.dp,
                                    shadowElevation = 8.dp,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    color = Color.White,
                                ) {
                                    Column {
                                        uiState.customerSuggestions.take(5).forEach { customer: com.example.servicemanager.core.domain.Customer ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.onCustomerNameChanged(customer.name)
                                                        viewModel.onCustomerPhoneChanged(customer.phone)
                                                        viewModel.onCustomerTypeChanged(customer.type)
                                                    }
                                                    .padding(16.dp),
                                            ) {
                                                Text(customer.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                                Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = DesignTokens.MutedInk)
                                            }
                                        }
                                    }
                                }
                            }
                            }
                            SentinelTextField(value = uiState.customerPhone, onValueChange = viewModel::onCustomerPhoneChanged, label = "Phone Number")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("CUSTOMER TYPE", style = MaterialTheme.typography.labelMedium, color = DesignTokens.MutedInk)
                                SegmentedChoiceRow(
                                    options = listOf("Individual", "Business", "Government"),
                                    selected = uiState.customerType,
                                    onSelect = viewModel::onCustomerTypeChanged,
                                )
                            }
                        }
                    }
                }

                DetailSection("02. Hardware Specification") {
                    SentinelCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SentinelDropdownField(
                                value = uiState.deviceType,
                                onValueChange = viewModel::onDeviceTypeChanged,
                                label = "Device Type",
                                options = deviceTypes.map { it.name },
                                placeholder = if (deviceTypes.isEmpty()) "No device types in settings" else "Select device type",
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                SentinelDropdownField(
                                    value = uiState.deviceBrand,
                                    onValueChange = viewModel::onBrandChanged,
                                    label = "Brand",
                                    options = brands.map { it.name },
                                    placeholder = if (brands.isEmpty()) "No brands in settings" else "Select brand",
                                    modifier = Modifier.weight(1f),
                                )
                                SentinelTextField(value = uiState.deviceModel, onValueChange = viewModel::onModelChanged, label = "Model", modifier = Modifier.weight(1f))
                            }
                            SentinelTextField(value = uiState.serialNumber, onValueChange = viewModel::onSerialChanged, label = "Serial Number")
                        }
                    }
                }

                DetailSection("03. Service Context") {
                    SentinelCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            IntentSelectionGroup(
                                selectedIntent = uiState.intent,
                                onIntentSelected = viewModel::onIntentChanged,
                            )
                            SentinelTextField(value = uiState.reportedProblem, onValueChange = viewModel::onProblemChanged, label = "Problem Description")
                        }
                    }
                }

                DetailSection("04. Logistics & Pricing") {
                    SentinelCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("PRIORITY LEVEL", style = MaterialTheme.typography.labelMedium, color = DesignTokens.MutedInk)
                                SegmentedChoiceRow(
                                    options = listOf("LOW", "MEDIUM", "HIGH", "URGENT"),
                                    selected = uiState.priority.name,
                                    onSelect = { viewModel.onPriorityChanged(PriorityLevel.valueOf(it)) },
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                SentinelTextField(value = uiState.estimate, onValueChange = viewModel::onEstimateChanged, label = "Initial Estimate (₹)", modifier = Modifier.weight(1f))
                                SentinelTextField(value = uiState.advance, onValueChange = viewModel::onAdvanceChanged, label = "Advance Paid (₹)", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                DetailSection("05. Visual Documentation") {
                    SentinelCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(80.dp)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = DesignTokens.MutedInk)
                                        Text("GALLERY", style = MaterialTheme.typography.labelSmall, color = DesignTokens.MutedInk)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(80.dp)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .clickable { /* Camera launcher logic */ },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = DesignTokens.MutedInk)
                                        Text("CAMERA", style = MaterialTheme.typography.labelSmall, color = DesignTokens.MutedInk)
                                    }
                                }
                            }

                            if (uiState.capturedImages.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    uiState.capturedImages.forEach { uri ->
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

private val repairIntentOptions = listOf(
    "NEW",
    "REPAIR",
    "WARRANTY",
    "DIAGNOSTIC",
)

@Composable
private fun IntentSelectionGroup(
    selectedIntent: String,
    onIntentSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("REPAIR INTENT", style = MaterialTheme.typography.labelMedium, color = DesignTokens.MutedInk)
        repairIntentOptions.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    .selectable(
                        selected = selectedIntent == option,
                        onClick = { onIntentSelected(option) },
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RadioButton(
                    selected = selectedIntent == option,
                    onClick = null,
                )
                Text(
                    text = option.replace('_', ' '),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DesignTokens.Ink,
                )
            }
        }
    }
}

@Composable
private fun CreateCustomerRoute(
    navController: NavHostController,
    viewModel: CreateCustomerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Success -> {
                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                    if (previousRoute == Routes.AddService) {
                        navController.previousBackStackEntry?.savedStateHandle?.set(RESULT_CUSTOMER_NAME, uiState.name)
                        navController.previousBackStackEntry?.savedStateHandle?.set(RESULT_CUSTOMER_PHONE, uiState.phone)
                        navController.previousBackStackEntry?.savedStateHandle?.set(RESULT_CUSTOMER_TYPE, uiState.customerType)
                    }
                    navController.popBackStack()
                }
                is UiEvent.Message -> snackbar.showSnackbar(event.text)
            }
        }
    }

    ModalSurface(
        title = "Create New Customer",
        subtitle = "Register a new entity in the service database",
        onDismiss = { navController.popBackStack() },
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SentinelTextField(value = uiState.name, onValueChange = viewModel::onNameChanged, label = "Full Name / Entity Name")
            SentinelTextField(value = uiState.phone, onValueChange = viewModel::onPhoneChanged, label = "Primary Contact Number")
            SentinelTextField(value = uiState.email, onValueChange = viewModel::onEmailChanged, label = "Email Address (Optional)")
            SentinelTextField(value = uiState.company, onValueChange = viewModel::onCompanyChanged, label = "Company / Department")
            SentinelTextField(value = uiState.address, onValueChange = viewModel::onAddressChanged, label = "Physical Address")

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ENTITY CLASSIFICATION", style = MaterialTheme.typography.labelMedium, color = DesignTokens.MutedInk)
                SegmentedChoiceRow(
                    options = listOf("Individual", "Business", "Government"),
                    selected = uiState.customerType,
                    onSelect = viewModel::onTypeChanged,
                )
            }

            PrimaryActionButton(
                text = if (uiState.isSaving) "SAVING..." else "CREATE CUSTOMER",
                onClick = viewModel::submit,
                enabled = !uiState.isSaving,
            )
        }
    }
}

@Composable
private fun SettingsRoute(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DesignTokens.Ink)
                        }
                        Text(
                            "Master Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DesignTokens.Ink,
                        )
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SettingsMenuCard("Profile Settings", "Company details and identity", Icons.Default.Settings) {
                    navController.navigate(Routes.SettingsProfile)
                }
                SettingsMenuCard("Manage Brands", "Brand catalog for devices", Icons.Default.Inventory2) {
                    navController.navigate(Routes.SettingsBrands)
                }
                SettingsMenuCard("Device Types", "Configure supported hardware", Icons.Default.LaptopMac) {
                    navController.navigate(Routes.SettingsDeviceTypes)
                }
                SettingsMenuCard("QC Checklist", "Manage quality control items", Icons.Default.Build) {
                    navController.navigate(Routes.SettingsQCChecklist)
                }
                SettingsMenuCard("Status Workflow", "SLA and status durations", Icons.Default.Timer) {
                    navController.navigate(Routes.SettingsStatusWorkflow)
                }
            }
        }
    }
}

@Composable
private fun SettingsProfileRoute(
    navController: NavHostController,
    viewModel: CompanyProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ModalSurface(
        title = "Profile Settings",
        subtitle = "Configure company profile and contact details",
        onDismiss = { navController.popBackStack() },
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SentinelTextField(
                value = uiState.companyName,
                onValueChange = viewModel::onCompanyNameChanged,
                label = "Company Name",
            )
            SentinelTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChanged,
                label = "Address",
                singleLine = false,
            )
            SentinelTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChanged,
                label = "Phone",
            )
            SentinelTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = "Email",
            )
            SentinelTextField(
                value = uiState.taxId,
                onValueChange = viewModel::onTaxIdChanged,
                label = "Tax ID / GSTIN",
            )
            SentinelTextField(
                value = uiState.otherDetails,
                onValueChange = viewModel::onOtherDetailsChanged,
                label = "Other Details",
                singleLine = false,
            )
            PrimaryActionButton(
                text = if (uiState.isSaving) "SAVING..." else "SAVE PROFILE",
                onClick = viewModel::save,
                enabled = !uiState.isSaving && uiState.companyName.isNotBlank(),
            )
        }
    }
}

@Composable
private fun SettingsMenuCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    SentinelCard(modifier = Modifier.clickable { onClick() }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DesignTokens.Ink.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = DesignTokens.Ink)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DesignTokens.MutedInk)
            }
        }
    }
}

@Composable
private fun SettingsBrandsRoute(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MasterDataListScreen(
        title = "Brands",
        items = uiState.brands.map { it.name },
        onAdd = viewModel::addBrand,
        onDelete = { name -> uiState.brands.find { it.name == name }?.let { viewModel.deleteBrand(it) } },
        onBack = { navController.popBackStack() }
    )
}

@Composable
private fun SettingsDeviceTypesRoute(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MasterDataListScreen(
        title = "Device Types",
        items = uiState.deviceTypes.map { it.name },
        onAdd = viewModel::addDeviceType,
        onDelete = { name -> uiState.deviceTypes.find { it.name == name }?.let { viewModel.deleteDeviceType(it) } },
        onBack = { navController.popBackStack() }
    )
}

@Composable
private fun SettingsQCChecklistRoute(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MasterDataListScreen(
        title = "QC Checklist",
        items = uiState.qcChecklist.map { it.title },
        onAdd = viewModel::addQCChecklistItem,
        onDelete = { title -> uiState.qcChecklist.find { it.title == title }?.let { viewModel.deleteQCChecklistItem(it) } },
        onBack = { navController.popBackStack() }
    )
}

@Composable
private fun MasterDataListScreen(
    title: String,
    items: List<String>,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit
) {
    var newItemName by remember { mutableStateOf("") }

    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DesignTokens.Ink)
                        }
                        Text(
                            title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DesignTokens.Ink,
                        )
                    }
                }
            },
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SentinelTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = "Add New",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                onAdd(newItemName)
                                newItemName = ""
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(DesignTokens.Ink)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(Modifier.height(24.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { item ->
                        SentinelCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                IconButton(onClick = { onDelete(item) }) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Red.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsStatusWorkflowRoute(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuredActive = uiState.statusConfigs.filter { it.isActive }.map { it.status }
    val activeStatuses = uiState.statusOrder.filter { it in configuredActive }
    val inactiveStatuses = uiState.statusOrder.filterNot { it in configuredActive }
    var statusInput by remember { mutableStateOf("") }
    val statusToAdd = remember(inactiveStatuses, statusInput) {
        inactiveStatuses.firstOrNull { it.matchesStatusInput(statusInput) }
    }
    var draftConfigs by remember(uiState.statusConfigs) {
        mutableStateOf(
            ServiceStatus.values().associateWith { status ->
                val config = uiState.statusConfigs.find { it.status == status }
                StatusConfigDraft(
                    minutes = (config?.estimatedMinutes ?: 0).toString(),
                    fixedTime = config?.fixedTimeOfDayMinutes?.toString() ?: "",
                    showQcWarning = config?.showQcWarningForIncomplete ?: false,
                )
            }
        )
    }

    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DesignTokens.Ink)
                        }
                        Text(
                            "Status Workflows",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DesignTokens.Ink,
                        )
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DetailSection("Workflow Configurations") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (inactiveStatuses.isNotEmpty()) {
                            SentinelCard {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Add Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    SentinelTextField(
                                        value = statusInput,
                                        onValueChange = { statusInput = it },
                                        label = "New Status",
                                        placeholder = "Type status (e.g. waiting for spare)",
                                    )
                                    if (statusInput.isNotBlank() && statusToAdd == null) {
                                        Text(
                                            text = "No inactive status matched. Available: ${inactiveStatuses.joinToString { it.name }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DesignTokens.MutedInk,
                                        )
                                    }
                                    PrimaryActionButton(
                                        text = "ADD STATUS",
                                        onClick = {
                                            statusToAdd?.let { status ->
                                                viewModel.setStatusActive(status, true)
                                                val reordered = (activeStatuses + status) + inactiveStatuses.filterNot { it == status }
                                                viewModel.setStatusOrder(reordered)
                                                statusInput = ""
                                            }
                                        },
                                        enabled = statusToAdd != null,
                                    )
                                }
                            }
                        }
                        activeStatuses.forEach { status ->
                            val draft = draftConfigs.getValue(status)
                            StatusConfigCard(
                                status = status,
                                minutes = draft.minutes,
                                fixedTime = draft.fixedTime,
                                showQcWarning = draft.showQcWarning,
                                onDelete = { viewModel.setStatusActive(status, false) },
                                onMoveUp = {
                                    val index = activeStatuses.indexOf(status)
                                    if (index > 0) {
                                        val moved = activeStatuses.toMutableList().apply {
                                            add(index - 1, removeAt(index))
                                        }
                                        viewModel.setStatusOrder(moved + inactiveStatuses)
                                    }
                                },
                                onMoveDown = {
                                    val index = activeStatuses.indexOf(status)
                                    if (index >= 0 && index < activeStatuses.lastIndex) {
                                        val moved = activeStatuses.toMutableList().apply {
                                            add(index + 1, removeAt(index))
                                        }
                                        viewModel.setStatusOrder(moved + inactiveStatuses)
                                    }
                                },
                                onMinutesChanged = { minutes ->
                                    draftConfigs = draftConfigs + (status to draft.copy(minutes = minutes))
                                },
                                onFixedTimeChanged = { fixed ->
                                    draftConfigs = draftConfigs + (status to draft.copy(fixedTime = fixed))
                                },
                                onShowQcWarningChanged = { show ->
                                    draftConfigs = draftConfigs + (status to draft.copy(showQcWarning = show))
                                },
                            )
                        }
                        PrimaryActionButton(
                            text = "SAVE ALL CONFIGS",
                            onClick = {
                                activeStatuses.forEach { status ->
                                    val draft = draftConfigs.getValue(status)
                                    viewModel.updateConfig(
                                        com.example.servicemanager.core.domain.ServiceStatusConfig(
                                            status = status,
                                            estimatedMinutes = draft.minutes.toIntOrNull() ?: 0,
                                            fixedTimeOfDayMinutes = draft.fixedTime.toIntOrNull(),
                                            showQcWarningForIncomplete = draft.showQcWarning,
                                            isActive = true,
                                        )
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusConfigCard(
    status: ServiceStatus,
    minutes: String,
    fixedTime: String,
    showQcWarning: Boolean,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMinutesChanged: (String) -> Unit,
    onFixedTimeChanged: (String) -> Unit,
    onShowQcWarningChanged: (Boolean) -> Unit,
) {
    SentinelCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(status.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = onMoveUp) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Move status up",
                            modifier = Modifier.size(18.dp),
                            tint = DesignTokens.Ink,
                        )
                    }
                    IconButton(onClick = onMoveDown) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Move status down",
                            modifier = Modifier.size(18.dp),
                            tint = DesignTokens.Ink,
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Remove status",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Red.copy(alpha = 0.7f),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SentinelTextField(
                    value = minutes,
                    onValueChange = onMinutesChanged,
                    label = "Est. Minutes",
                    modifier = Modifier.weight(1f)
                )
                SentinelTextField(
                    value = fixedTime,
                    onValueChange = onFixedTimeChanged,
                    label = "Fixed Time (Mins from midnight)",
                    modifier = Modifier.weight(1f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SHOW QC WARNING", style = MaterialTheme.typography.labelMedium, color = DesignTokens.MutedInk)
                SegmentedChoiceRow(
                    options = listOf("YES", "NO"),
                    selected = if (showQcWarning) "YES" else "NO",
                    onSelect = { onShowQcWarningChanged(it == "YES") },
                )
            }
        }
    }
}

@Composable
private fun InvoiceRoute(
    navController: NavHostController,
    viewModel: InvoiceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color.White,
                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DesignTokens.Ink)
                        }
                        Text(
                            "Invoices",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DesignTokens.Ink,
                        )
                    }
                }
            },
        ) { padding ->
            if (uiState.invoices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No invoices found.", style = MaterialTheme.typography.bodyLarge, color = DesignTokens.MutedInk)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.invoices) { invoice ->
                        SentinelCard {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("#${invoice.serviceCode}", style = MaterialTheme.typography.labelSmall, color = DesignTokens.MutedInk)
                                        Text(invoice.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Text(formatInr(invoice.amount), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(formatTimestamp(invoice.createdAt), style = MaterialTheme.typography.bodySmall, color = DesignTokens.MutedInk)
                                    StatusBadge(status = invoice.status)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SecondaryActionButton(text = "VIEW", onClick = { /* TODO */ }, modifier = Modifier.weight(1f))
                                    SecondaryActionButton(text = "DELETE", onClick = { viewModel.removeInvoice(invoice.id) }, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: InvoiceStatus) {
    val color = when (status) {
        InvoiceStatus.PAID -> Color(0xFF4CAF50)
        InvoiceStatus.SENT -> Color(0xFF2196F3)
        InvoiceStatus.DRAFT -> Color(0xFF9E9E9E)
        InvoiceStatus.VOID -> Color(0xFFF44336)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, color)
    ) {
        Text(
            status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
