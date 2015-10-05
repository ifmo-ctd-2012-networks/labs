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

By initial design, algo supports up to 2^14 participants, but it can be easily adjusted for bigger amount.

## Node states

Protocol consists of several procedures, operating on fixed set of node states

Each node, participating in communication can be only in a single state:

  * orphan
  * waiter
  * leader

In next sections we describe each state in turn, referencing procedures available for use from these states. Later we describe procedures in detail.

Basic concept of Jitterbug protocol is to maintain a minimal set of subnets (i.e. connected graphs of nodes) with proper support them to be splitted/merged.

In each state, node maintains following *state variables* (updated only in node's leader state)

  * last msg_id
  * last token_id
  * last sent data
  * list of nodes
  * set of nodes to add
  * nodes' penalties

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

It's launched by node, being in orphan state. For sender algo is following:

**token_restore_try** (*tryout_token_id*):

  1. Repeatedly send a UDP broadcast with message < TR1, last msg_id, *tryout_token_id* >
      * repeat interval is {tr_interval}
      * should repeat {tr_count} times
  2. Wait {tr_count}*{tr_interval} time for replies
      * replies would be of kind < TR2, msg_id, token_id >
  3. Analize replies
      * if there exist a tuple < msg_id, token_id > greater, than ours < last msg_id, *tryout_token_id* > (lexicographicaly)
          * **return false**
      * if there exist no such tuple
          * **return false**

**token_restore** ():
  
  1. access_granted_1 = **token_restore_try** (last_token_id) 
      * //try to grab access on leadership with last token id
  2. if (access_granted_1)
      1. last_token_id = generate_new_token_id ()
          *  //generate new random token_id
      2. access_granted_2 = **token_restore_try** (last_token_id) 
          * //try to ensure we still have rights for leadership, i.e. there still exist no tuple greater after token_id generation
      3. if (access_granted_2)
          * switch state to *leader*
      4. else
          * switch state to *waiter*
  3. switch state to *waiter*

All other nodes should do following on receiving of < TR1, msg_id, token_id > (for each message received):

  1. if tuple < msg_id, token_id > is greater, than ours, do nothing
  2. otherwise
      1. send < TR2, last msg_id, last token_id > as a reply (via UDP, only to sender's IP address)
      2. remember node to be later added to node list

See **Appendix A** section for some additional remarks regarding **token_restore** procedure (explanation of why it won't end up into infinite loop).

### token_pass

Token pass procedure's purpose is to pass token from current leader to next node in a list.

It's launched by current leader when he's ready to pass token. It's split into two phases:
  
  1. Passing up-to-date node list to next node
  2. Passing token

Further in this section we will refer to next node as candidate.
All communications, described bellow are done via TCP.

More detailed, for a single candidate:

#### *token_pass_for_candidate ( candidate_i )*:

  0. Execute within timeout {token_pass_timeout}
    1. Leader passes message < TP1, msg_id, token_id, node_list_hash > to candidate
        1. If candidate's < msg_id, token_id > are greater than leader's, it replies with < TP2 >, indicating that leader's token is outdated
           * **return true**
        2. otherwise candidate checks node_list_hash with hash of his node list and replies:
           1. < TP3 >, if hashs differ
              1. Leader sends message < TP5, node_list >
              2. Candidate remembers node_list for the connection (but doesn't update variables)
           2. < TP4 >, if hashs are equal. This case, candidate remembers node_list for the connection
    2. Leader passes a message < TP6, msg_id, token_id, data > to candidate
      * token was passed
    3. Leader updates node_list variable with updated penalties (see bellow)
    4. **return true**
  1. Timeut ticked, **return false**

Aforementioned algo is repeatedly tried for all candidates in turn (following node list from current node). We will describe this in detail after describing penalties (which play a key role in process of candidate selection).

#### Candidate selection and penalties

Every node locally stores a list of penalties for each node, participating in communication. Initially for every node:
  * penalty_threshold = 0
  * penalty_count = 0

The key idea of penalties is to disallow assumed-to-be-dead nodes from communication (not to waste time on them). Also, if node recover, we would like it to join the conversation again.
Let's consider candidate selection procedure. Assume we want to try candidate with ordinal number *i*:

First, we check for is candidate allowed to participate in current round:

  0. is_allowed_for_round = true
  1. if (penalty_count_i >= 2^penalty_threshold_i)
      1. candidate was disallowed for enough time
      2. update: penalty_count_i = 0
  2. else
      1. is_allowed_for_round = false
  3. if (is_allowed_for_round)
      1. res = **token_pass_for_candidate** ( candidate_i )
      2. if (res)
          1. if (penalty_threshold_i > 0) penalty_threshold_i--
      3. else
          1. penalty_threshold_i++
  4. else
      1. penalty_count_i++

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

Node list hash is a standard polynomial hash on base of 577.

message_id, token_id are 4-byte integers. token_id is randomly generated (in *token_restore* procedure)

Data is sent naturally, as byte sequence (prepended with 4-byte size of block being sent).

## Appendix A

Some remarks on **token_restore** procedure. From it's description it may seem, that it may fall into infinite loop. Now we will proof, why this won't happen.

First, let's note, how token_id numbers are generated. They are generally random numbers. Of course, in real environments their randomness is doubtfull. This property depends tightly on internals of random functions accross all nodes in system, seeds used to initialize these functions and so on. But in this section let's assume that distribution of generated numbers accross all nodes is close to uniform.

Given a uniform distribution accross all generated numbers, it's not hard to find upper bound of expected value of times, *token_pass* procedure would be launched before any node finally takes leadership (we consider now a single subnet, no merges/splits with other subnets occur).

If any node has msg_id, greater than anyone else has, this node will become a leader when it would launch **token_restore** procedure in one step, so let's consider only case, when more than one node has maximal msg_id.


If our < msg_id, token_id > is greater than any other < msg_id, token_id > in system, we take the leadership. Otherwise there exist at least one node, whoes msg_id is equal to ours and token_id is greater. Token_ids are uniformly distributed within range 0..2^32-1, so possibility that one lower or equal, than other is 1/2. So with possibility 1/2 **token_restore** would be launched only once.

Same reasoning could be applied to further steps, which directly implies possibility for k-th launch to succeed as 1/2^i.

Taking sum of series 1/2^i from i=0 to oo, we conclude to expected value of **token_restore** launches be not greater than 2.

Actually, it's lower, cause we considered only case with equal msg_ids. Considered that in most cases max(msg_id) is held by only node in subnet, expected value of launches would be even less.
