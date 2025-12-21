---
title: "Quantum systems that feel like code"
date: "2024-09-19"
category: "science"
tags: ["quantum", "experiments", "signal"]
summary: "Notes on wiring lab-grade measurements with programmer-minded tooling."
---
# Quantum signal scaffolding
The lab notebook is turning into a code repo. Every measurement is a commit, and every waveform is a diff.

## Interfaces over instruments
- Wrap oscilloscopes with tiny adapters.
- Move configuration into readable data files.
- Keep the feedback loop short; graphs should recompile as fast as code.

## Field notes
When the laser drifts, the log should tell you which script changed. Traceability beats heroics.
