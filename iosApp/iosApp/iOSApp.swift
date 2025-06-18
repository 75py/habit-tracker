import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        // Initialize Koin at app startup
        KoinInitKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}