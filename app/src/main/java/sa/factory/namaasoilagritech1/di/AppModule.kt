package sa.factory.namaasoilagritech1.di

import sa.factory.namaasoilagritech1.data.repository.NamaaSoilRepositoryImpl
import sa.factory.namaasoilagritech1.domain.repository.NamaaSoilRepository
import sa.factory.namaasoilagritech1.domain.usecase.*
import sa.factory.namaasoilagritech1.hardware.HardwareSensorDriver
import sa.factory.namaasoilagritech1.ui.viewmodel.NamaaSoilViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: NamaaSoilRepository = NamaaSoilRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): NamaaSoilViewModel = NamaaSoilViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
