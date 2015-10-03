---
title: Jitterbug protocol
permalink: jitterbug.html
---
[[Index]](index.html)

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
   * This sequence is distributed and can be determined only by getting information from all nodes, occured in communication

## Node states

Protocol consists of several procedures, operating on fixed set of node states

Each node, participating in communication can be only in a single state:

  * orphan
  * waiter
  * leader

In next sections we describe each state in turn, referencing procedures available for use from these states. Later we describe procedures in detail.

Basic concept of Jitterbug protocol is to maintain a minimal set of subnets (i.e. connected graphs of nodes) with proper support them to be splitted/merged.

{% xdot png %}
digraph jitterbug {
  orphan [label="Orphan"];
  leader [label="Leader"];
  waiter [label="Waiter"];

  orphan -> leader [label="*token_restore*"];
  orphan -> waiter [label="*token_restore*"];
  waiter -> orphan [label="{renew_timeout}"];
  leader -> waiter [label="token passed"];
  waiter -> leader [label="token received"];

}
{% endxdot %}

In each state, node maintains following *state variables* (updated only in node's leader state)

  * last msg_id
  * last token_id
  * last list of active nodes

### Orphan state

Being orphan means that you are not associated yet with any subnet.
Node can have orphan state only in two cases:

 * when it was just initiated, i.e. haven't yet participated in any communication with other nodes
 * after {renew_timeout} occured, i.e. node realizes that it haven't received messages but for too long

When node find itself an orphan, it tries to join active subnet (or create own if no yet exist).
To do so, node initiates *token_restore* procedure, after executing which it switches state to one of:

  * waiter
  * leader

### Waiter state

It's a passive state of algo. In this state node waits for either of events to occur:
  
  * {renew_timeout} occurs, node becomes an orphan
  * token received, node becomes a leader

### Leader
It's an active state of node. Being in this state node follows such flow:

  1. Computes next message
  2. Updates *state variables*:
      1. increment msg_id
      2. updates node list with new nodes, not yet in the list
  3. Launchs *token_pass* procedure

## Procedures

### token_restore

Token restore procedure's purpose is to get node acknowledged of current active token status.

It's launched by node, being in orphan state. For this node algo is following:

  1. Repeatedly send a UDP broadcast with message < TR1, last msg_id, last token_id >
      * repeat interval is {tr_interval}
      * should repeat {tr_count} times
  2. Wait {tr_count}*{tr_interval} time for replies
      * replies would be of kind < TR2, msg_id, token_id >
  3. Analize replies
      * if there exist a tuple < msg_id, token_id > greater, than ours < last msg_id, last token_id > (lexicographicaly), do nothing
      * if there exist no such tuple, become a *leader*
        * before becoming a leader, generate new token_id

All other nodes should do following on receiving of < TR1, msg_id, token_id > (for each message received):
  1. if tuple < msg_id, token_id > is greater, than ours, do nothing
  2. otherwise
      1. send < TR2, last msg_id, last token_id > as a reply (via UDP, only to sender's IP address)
      2. remember node to be later added to node list


### token_pass

Token pass procedure's purpose is to pass token from current leader to next node in a list.

It's launched by current leader when he's ready to pass token. It's split into two phases:
  
  1. Passing up-to-date node list to next node
  2. Passing token

Further in this section we will refer to next node as candidate.
All communications, described bellow are done via TCP.

More detailed, for a single candidate:

  1. Leader passes message < TP1, msg_id, token_id, node_list_hash > to candidate
      1. If candidate's < msg_id, token_id > are greater than leader's, it replies with < TP2 >, indicating that leader's token is outdated
         * procedure has been performed
      2. otherwise candidate checks node_list_hash with hash of his node list and replies:
         1. < TP3 >, if hashs differ
            1. Leader sends message < TP5, node_list >
            2. Candidate remembers node_list for the connection (but doesn't update variables)
         2. < TP4 >, if hashs are equal. This case, candidate remembers node_list for the connection
  2. Leader passes a message < TP6, msg_id, token_id > to candidate
      * procedure has been performed
  3. Leader updates node_list variable with updated penalties (see bellow)

Aforementioned algo is repeatedly tried for all candidates in turn (following node list from current node):

  * procedure is tried only for nodes, whoes penalties allow this (penalty_count >= 2^penalty_threshold)
      * if candidate is not allowed, penalty_count--
  * if procedure has been performed for any candidate, *token_pass* terminates
    * if penalty_threshold > 0, penalty_threshold--
  * if procedure fails for candidate
      * penalty_threshold++

## Messages and variables
  
Each message < type, a, b, c, d >:

 * first byte contains version and type, four bits for each
 * rest of parts a, b, c, d are serialized to bytes and written in turn

Type constants:
  
  * TR1 = 0
  * TR2 = 1
  * TP1 = 2
  * TP2 = 3
  * TP3 = 4
  * TP4 = 5
  * TP5 = 6
  * TP6 = 7

Node list:

  * two-byte size of list
  * nodes in format:
     * ip address, 4 bytes
     * penalty_threshold
     * penalty_threshold

Node list hash is a standard polynomial hash on base of 577.

message_id, token_id are 4-byte integers. token_id is randomly generated (in *token_restore* procedure)


