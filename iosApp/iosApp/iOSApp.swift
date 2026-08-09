import SwiftUI
import Shared

@main
struct iOSApp: App {
    private let dependencies: AppDependencies

    init() {
        dependencies = AppDependencies()
        dependencies.configure()
    }

    var body: some Scene {
        WindowGroup {
            dependencies.makeContentView()
        }
    }
}

private struct AppDependencies {
    func configure() {
        KoinKt.doInitKoin()
    }

    func makeContentView() -> ContentView {
        ContentView(makeRootViewController: MainViewControllerKt.mainViewController)
    }
}
