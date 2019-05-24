# Introduction

This is an experiment to validate pulsar with chaos testing applied with the test containers project and it's toxiproxy module.

## Simulating High latency pub sub
```
Publish throughput: 0.28 msg/s --- 0.00 Mbit/s --- Latency: med: 3250.491 ms - 95pct: 3462.378 ms - 99pct: 3462.378 ms - 99.9pct: 3462.378 ms - max: 3462.378 ms --- Ack received rate: 0.27 ack/s --- Failed messages: 0
Publish throughput: 0.32 msg/s --- 0.00 Mbit/s --- Latency: med: 3194.156 ms - 95pct: 3513.389 ms - 99pct: 3513.389 ms - 99.9pct: 3513.389 ms - max: 3513.389 ms --- Ack received rate: 0.32 ack/s --- Failed messages: 0
Publish throughput: 0.30 msg/s --- 0.00 Mbit/s --- Latency: med: 3175.359 ms - 95pct: 3399.856 ms - 99pct: 3399.856 ms - 99.9pct: 3399.856 ms - max: 3399.856 ms --- Ack received rate: 0.30 ack/s --- Failed messages: 0
Publish throughput: 0.32 msg/s --- 0.00 Mbit/s --- Latency: med: 3220.388 ms - 95pct: 3392.110 ms - 99pct: 3392.110 ms - 99.9pct: 3392.110 ms - max: 3392.110 ms --- Ack received rate: 0.32 ack/s --- Failed messages: 0
```