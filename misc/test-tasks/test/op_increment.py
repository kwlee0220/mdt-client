from __future__ import annotations

import sys, time
import time
import signal
import argparse

def signal_handler(sig, frame):
  print(f"Task interrupted (singal={sig}).")
  sys.exit(0)
signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)

parser = argparse.ArgumentParser(description="Test")
parser.add_argument('data', metavar='path for the data')
parser.add_argument('--inc', metavar='amount', type=int, required=True)
parser.add_argument('--sleep', metavar='seconds', type=int, required=True)
parser.add_argument('--output', metavar='path', required=True)
args = parser.parse_args()

with open(args.data, 'r') as file:
  data = file.read()

data = int(data)
print(f'data: {data}')
print(f'increment: {args.inc}')

print(f"Sleeping {args.sleep} seconds", flush=True)
time.sleep(args.sleep)

data += args.inc
print(f'output: {data}', flush=True)

with open(args.output, "w") as file:
  file.write(f'{data}')

print("Done.")
