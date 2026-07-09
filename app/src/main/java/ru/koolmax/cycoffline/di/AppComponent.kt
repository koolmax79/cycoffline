package ru.koolmax.cycoffline.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.koolmax.cycoffline.data.db.FitDatabase
import javax.inject.Singleton

//@Component(modules = [AppModule::class])
//interface AppComponent {
//    val fitViewModel: FitViewModel
//    val calendarViewModel: CalendarViewModel
//    val deviceListViewModel: DeviceListViewModel
//    val statisticsViewModel: StatisticsViewModel
//    //val chartListViewModel: ChartListViewModel
//    val settingsRepository: SettingsRepository
//}


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

//    @Provides
//    @Singleton
//    fun provideSettingsRepository(context: Context) = SettingsRepository(context)

//    @Provides
//    @Singleton
//    fun provideFileRepository(@ApplicationContext context: Context) = FileRepository(context)

//    @Provides
//    @Singleton
//    fun provideDeviceRepository(@ApplicationContext context: Context) = DeviceRepository(context)

//    @Singleton
//    @Provides
//    fun provideFitRepository(db: FitDatabase): FitRepository {
//        return FitRepository(db)
//    }

    @Singleton
    @Provides
    fun provideFitDatabase(@ApplicationContext context: Context): FitDatabase {
        return FitDatabase.invoke(context)
    }
}