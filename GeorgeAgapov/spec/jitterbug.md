---
title: Jitterbug protocol
permalink: /jitterbug/
---
[[Index]](/)

# Jitterbug protocol

Jitterbug protocol is a yet another protocol for token-ring emulation.

It provides following garantees:

 * Liveness - if at least two nodes are alive, communication will not stop
   * Case with dead leader is handled via specific procedure, that decides, who should be a new token holder
 * Weak token-ring list consistency between nodes:
   * Only current leader (token holder) can add a new node to the list
   * No node can be deleted from the list
      * Dead nodes are handled via penalties
   * Up-to-date version of node list is transferred with token
 * Messages are enumerated, algo maintains chains of messages with sequent numbers with no repeats
   * Generally, algo tries to maintain a single chain, but eventually extra chains may arise
   * For any message, sequence of preceeding messages can be strictly determined
   * This sequence is distributed and can be determined only by getting information from all nodes, occurred in communication

## Protocol overview

