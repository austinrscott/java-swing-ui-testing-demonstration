#!/usr/bin/env python3
from xmlrpc.server import SimpleXMLRPCServer, SimpleXMLRPCRequestHandler
import pprint
import datetime

class RequestHandler(SimpleXMLRPCRequestHandler):
  rpc_paths = ("/RPC2",)

def receive(payload):
  # payload is expected to be a dict with 'userId', 'amount', 'currency'
  print("=== XML-RPC payload received ===")
  pprint.pprint(payload)
  print(f"Timestamp: {datetime.datetime.now().isoformat()}")
  user = payload.get("userId", "<unknown>")
  amount = payload.get("amount", 0)
  currency = payload.get("currency", "")
  return f"Server received: userId={user}, amount={amount} {currency}".strip()

def main():
  host = "127.0.0.1"
  port = 7777
  with SimpleXMLRPCServer((host, port), requestHandler=RequestHandler, allow_none=False, logRequests=True) as server:
    server.register_function(receive, "receive")
    server.register_introspection_functions()  # enables system.listMethods
    print(f"XML-RPC server listening on http://{host}:{port}/RPC2")
    print("Method: receive(payload: struct) -> string")
    print("Press Ctrl+C to stop.")
    try:
      server.serve_forever()
    except KeyboardInterrupt:
      print("\nShutting down...")

if __name__ == "__main__":
  main()