# Swing UI Testing Demo

This project demonstrates how to structure Swing UIs for testability, how to mock external services, and how to write
both unit and robot-driven UI tests.

## Requirements
- Python 3.7+
- JDK 21+
    - You can install on Ubuntu with `sudo apt update && sudo apt install -y openjdk-21-jdk`
- Apache Maven 3.6.3 (older versions might work, but I haven't tested them)

## What you’ll learn

- Separate business logic from UI (Presenter/Calculator vs. Swing Panel).
- Mock external services (e.g., XML-RPC) for fast, deterministic tests.
- Keep the EDT (Event Dispatching Thread) responsive by running work on background executors.
- Write UI tests with AssertJ Swing and avoid flakiness.

### Architecture at a glance

- TriadCalculator: pure logic for 3 values (A, B, C) that must sum to a total.
- TriadPresenter: parses/validates input, calls calculator, notifies the view.
- TriadPanel: thin Swing view, forwards events to Presenter, updates fields on EDT.
- RpcClient (interface): abstraction for sending a payload to an external service.
- SendPresenter: validates inputs, builds payload, calls RpcClient asynchronously.
- SendPanel: thin Swing view for the send form.
- Main: manual runner; wires real/mock services at the app edge.

### Threading rules (important for Swing)

- All UI updates must happen on the EDT (SwingUtilities.invokeLater).
- Long-running or blocking work must NOT run on the EDT (use Executor/ExecutorService).
- Presenters should be UI-agnostic; Views should avoid business logic.

### Testing strategy

- Unit tests (fast):
    - TriadCalculatorTest: logic only, no Swing.
    - TriadPresenterTest: validation and update flow, no Swing.
    - SendPresenterTest: payload composition, success/error, using mocked RpcClient.
- UI tests (robot):
    - TriadPanelUiTest: simulate typing and verify updates.
    - SendPanelUiTest: fill fields, click, await status, with mocked RpcClient.

### Mocking external services

- Presenters depend on interfaces (RpcClient), not concrete network code.
- Tests mock RpcClient to verify payloads and simulate success/error.
- For manual demos, run the small Python XML-RPC server in one tab (xmlrpc_server.py) and run Main in the other.
    - From the root directory: `python xmlrpc_server.py`

## Running

- From the `hello-io/` directory:
    - Build: `mvn compile`
    - Run Tests: `mvn test`
    - Play with the program manually: `mvn exec:java -Dexec.mainClass=com.example.Main`
- Onscreen Tabs:
    - Triad: play with three inputs and the total spinner. The 3 inputs always add up to the total, no matter how you
      edit it.
    - Send XMLRPC: enter data and click Send (wire to real or mock client).

## Tips to avoid flaky UI tests

- Give components stable names (setName) for robust selectors.
- Use Awaitility to wait for UI conditions instead of sleeps.
- Ensure tests run headful (`-Djava.awt.headless=false`).
- On JDK 21, add VM options to open JDK internals for AssertJ Swing:
    - `--add-opens java.base/java.util=ALL-UNNAMED`
    - `-XX:+EnableDynamicAgentLoading`
