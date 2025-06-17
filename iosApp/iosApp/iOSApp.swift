import SwiftUI
import ComposeApp
import UserNotifications

@main
struct iOSApp: App {
    
    private let notificationDelegate = NotificationDelegate()
    
    init() {
        // Initialize Koin at app startup
        KoinInitKt.doInitKoin()
        
        // Set up notification delegate
        setupNotificationDelegate()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
    private func setupNotificationDelegate() {
        let center = UNUserNotificationCenter.current()
        center.delegate = notificationDelegate
        
        // Also initialize notification categories
        IOSNotificationSchedulerHelperKt.setupNotificationCategories()
    }
}