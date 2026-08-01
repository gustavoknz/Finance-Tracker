import SwiftUI
import UIKit

typealias RootViewControllerFactory = () -> UIViewController

struct ContentView: View {
    private let makeRootViewController: RootViewControllerFactory

    init(makeRootViewController: @escaping RootViewControllerFactory) {
        self.makeRootViewController = makeRootViewController
    }

    var body: some View {
        RootViewControllerRepresentable(makeViewController: makeRootViewController)
            .ignoresSafeArea()
    }
}

private struct RootViewControllerRepresentable: UIViewControllerRepresentable {
    let makeViewController: RootViewControllerFactory

    func makeUIViewController(context: Context) -> UIViewController {
        makeViewController()
    }

    func updateUIViewController(_ viewController: UIViewController, context: Context) {}
}
