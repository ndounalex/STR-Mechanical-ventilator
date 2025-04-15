package com.udes.utils;

public class Queue {

    public static String blockingconcurrentqueue = "// Provides an efficient blocking version of moodycamel::ConcurrentQueue.\n" +
            "// ©2015-2020 Cameron Desrochers. Distributed under the terms of the simplified\n" +
            "// BSD license, available at the top of concurrentqueue.h.\n" +
            "// Also dual-licensed under the Boost Software License (see LICENSE.md)\n" +
            "// Uses Jeff Preshing's semaphore implementation (under the terms of its\n" +
            "// separate zlib license, see lightweightsemaphore.h).\n" +
            "\n" +
            "#pragma once\n" +
            "\n" +
            "#include \"concurrentqueue.h\"\n" +
            "#include \"lightweightsemaphore.h\"\n" +
            "\n" +
            "#include <type_traits>\n" +
            "#include <cerrno>\n" +
            "#include <memory>\n" +
            "#include <chrono>\n" +
            "#include <ctime>\n" +
            "\n" +
            "namespace moodycamel\n" +
            "{\n" +
            "// This is a blocking version of the queue. It has an almost identical interface to\n" +
            "// the normal non-blocking version, with the addition of various wait_dequeue() methods\n" +
            "// and the removal of producer-specific dequeue methods.\n" +
            "template<typename T, typename Traits = ConcurrentQueueDefaultTraits>\n" +
            "class BlockingConcurrentQueue\n" +
            "{\n" +
            "private:\n" +
            "\ttypedef ::moodycamel::ConcurrentQueue<T, Traits> ConcurrentQueue;\n" +
            "\ttypedef ::moodycamel::LightweightSemaphore LightweightSemaphore;\n" +
            "\n" +
            "public:\n" +
            "\ttypedef typename ConcurrentQueue::producer_token_t producer_token_t;\n" +
            "\ttypedef typename ConcurrentQueue::consumer_token_t consumer_token_t;\n" +
            "\t\n" +
            "\ttypedef typename ConcurrentQueue::index_t index_t;\n" +
            "\ttypedef typename ConcurrentQueue::size_t size_t;\n" +
            "\ttypedef typename std::make_signed<size_t>::type ssize_t;\n" +
            "\t\n" +
            "\tstatic const size_t BLOCK_SIZE = ConcurrentQueue::BLOCK_SIZE;\n" +
            "\tstatic const size_t EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD = ConcurrentQueue::EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD;\n" +
            "\tstatic const size_t EXPLICIT_INITIAL_INDEX_SIZE = ConcurrentQueue::EXPLICIT_INITIAL_INDEX_SIZE;\n" +
            "\tstatic const size_t IMPLICIT_INITIAL_INDEX_SIZE = ConcurrentQueue::IMPLICIT_INITIAL_INDEX_SIZE;\n" +
            "\tstatic const size_t INITIAL_IMPLICIT_PRODUCER_HASH_SIZE = ConcurrentQueue::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE;\n" +
            "\tstatic const std::uint32_t EXPLICIT_CONSUMER_CONSUMPTION_QUOTA_BEFORE_ROTATE = ConcurrentQueue::EXPLICIT_CONSUMER_CONSUMPTION_QUOTA_BEFORE_ROTATE;\n" +
            "\tstatic const size_t MAX_SUBQUEUE_SIZE = ConcurrentQueue::MAX_SUBQUEUE_SIZE;\n" +
            "\t\n" +
            "public:\n" +
            "\t// Creates a queue with at least `capacity` element slots; note that the\n" +
            "\t// actual number of elements that can be inserted without additional memory\n" +
            "\t// allocation depends on the number of producers and the block size (e.g. if\n" +
            "\t// the block size is equal to `capacity`, only a single block will be allocated\n" +
            "\t// up-front, which means only a single producer will be able to enqueue elements\n" +
            "\t// without an extra allocation -- blocks aren't shared between producers).\n" +
            "\t// This method is not thread safe -- it is up to the user to ensure that the\n" +
            "\t// queue is fully constructed before it starts being used by other threads (this\n" +
            "\t// includes making the memory effects of construction visible, possibly with a\n" +
            "\t// memory barrier).\n" +
            "\texplicit BlockingConcurrentQueue(size_t capacity = 6 * BLOCK_SIZE)\n" +
            "\t\t: inner(capacity), sema(create<LightweightSemaphore, ssize_t, int>(0, (int)Traits::MAX_SEMA_SPINS), &BlockingConcurrentQueue::template destroy<LightweightSemaphore>)\n" +
            "\t{\n" +
            "\t\tassert(reinterpret_cast<ConcurrentQueue*>((BlockingConcurrentQueue*)1) == &((BlockingConcurrentQueue*)1)->inner && \"BlockingConcurrentQueue must have ConcurrentQueue as its first member\");\n" +
            "\t\tif (!sema) {\n" +
            "\t\t\tMOODYCAMEL_THROW(std::bad_alloc());\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\tBlockingConcurrentQueue(size_t minCapacity, size_t maxExplicitProducers, size_t maxImplicitProducers)\n" +
            "\t\t: inner(minCapacity, maxExplicitProducers, maxImplicitProducers), sema(create<LightweightSemaphore, ssize_t, int>(0, (int)Traits::MAX_SEMA_SPINS), &BlockingConcurrentQueue::template destroy<LightweightSemaphore>)\n" +
            "\t{\n" +
            "\t\tassert(reinterpret_cast<ConcurrentQueue*>((BlockingConcurrentQueue*)1) == &((BlockingConcurrentQueue*)1)->inner && \"BlockingConcurrentQueue must have ConcurrentQueue as its first member\");\n" +
            "\t\tif (!sema) {\n" +
            "\t\t\tMOODYCAMEL_THROW(std::bad_alloc());\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Disable copying and copy assignment\n" +
            "\tBlockingConcurrentQueue(BlockingConcurrentQueue const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\tBlockingConcurrentQueue& operator=(BlockingConcurrentQueue const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\t\n" +
            "\t// Moving is supported, but note that it is *not* a thread-safe operation.\n" +
            "\t// Nobody can use the queue while it's being moved, and the memory effects\n" +
            "\t// of that move must be propagated to other threads before they can use it.\n" +
            "\t// Note: When a queue is moved, its tokens are still valid but can only be\n" +
            "\t// used with the destination queue (i.e. semantically they are moved along\n" +
            "\t// with the queue itself).\n" +
            "\tBlockingConcurrentQueue(BlockingConcurrentQueue&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t\t: inner(std::move(other.inner)), sema(std::move(other.sema))\n" +
            "\t{ }\n" +
            "\t\n" +
            "\tinline BlockingConcurrentQueue& operator=(BlockingConcurrentQueue&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t{\n" +
            "\t\treturn swap_internal(other);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Swaps this queue's state with the other's. Not thread-safe.\n" +
            "\t// Swapping two queues does not invalidate their tokens, however\n" +
            "\t// the tokens that were created for one queue must be used with\n" +
            "\t// only the swapped queue (i.e. the tokens are tied to the\n" +
            "\t// queue's movable state, not the object itself).\n" +
            "\tinline void swap(BlockingConcurrentQueue& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t{\n" +
            "\t\tswap_internal(other);\n" +
            "\t}\n" +
            "\t\n" +
            "private:\n" +
            "\tBlockingConcurrentQueue& swap_internal(BlockingConcurrentQueue& other)\n" +
            "\t{\n" +
            "\t\tif (this == &other) {\n" +
            "\t\t\treturn *this;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinner.swap(other.inner);\n" +
            "\t\tsema.swap(other.sema);\n" +
            "\t\treturn *this;\n" +
            "\t}\n" +
            "\t\n" +
            "public:\n" +
            "\t// Enqueues a single item (by copying it).\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or implicit\n" +
            "\t// production is disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE is 0,\n" +
            "\t// or Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool enqueue(T const& item)\n" +
            "\t{\n" +
            "\t\tif ((details::likely)(inner.enqueue(item))) {\n" +
            "\t\t\tsema->signal();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by moving it, if possible).\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or implicit\n" +
            "\t// production is disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE is 0,\n" +
            "\t// or Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool enqueue(T&& item)\n" +
            "\t{\n" +
            "\t\tif ((details::likely)(inner.enqueue(std::move(item)))) {\n" +
            "\t\t\tsema->signal();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by copying it) using an explicit producer token.\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or\n" +
            "\t// Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool enqueue(producer_token_t const& token, T const& item)\n" +
            "\t{\n" +
            "\t\tif ((details::likely)(inner.enqueue(token, item))) {\n" +
            "\t\t\tsema->signal();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by moving it, if possible) using an explicit producer token.\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or\n" +
            "\t// Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool enqueue(producer_token_t const& token, T&& item)\n" +
            "\t{\n" +
            "\t\tif ((details::likely)(inner.enqueue(token, std::move(item)))) {\n" +
            "\t\t\tsema->signal();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues several items.\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or\n" +
            "\t// implicit production is disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE\n" +
            "\t// is 0, or Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Note: Use std::make_move_iterator if the elements should be moved instead of copied.\n" +
            "\t// Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline bool enqueue_bulk(It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\tif ((details::likely)(inner.enqueue_bulk(std::forward<It>(itemFirst), count))) {\n" +
            "\t\t\tsema->signal((LightweightSemaphore::ssize_t)(ssize_t)count);\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues several items using an explicit producer token.\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails\n" +
            "\t// (or Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Note: Use std::make_move_iterator if the elements should be moved\n" +
            "\t// instead of copied.\n" +
            "\t// Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline bool enqueue_bulk(producer_token_t const& token, It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\tif ((details::likely)(inner.enqueue_bulk(token, std::forward<It>(itemFirst), count))) {\n" +
            "\t\t\tsema->signal((LightweightSemaphore::ssize_t)(ssize_t)count);\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by copying it).\n" +
            "\t// Does not allocate memory. Fails if not enough room to enqueue (or implicit\n" +
            "\t// production is disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE\n" +
            "\t// is 0).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool try_enqueue(T const& item)\n" +
            "\t{\n" +
            "\t\tif (inner.try_enqueue(item)) {\n" +
            "\t\t\tsema->signal();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by moving it, if possible).\n" +
            "\t// Does not allocate memory (except for one-time implicit producer).\n" +
            "\t// Fails if not enough room to enqueue (or implicit production is\n" +
            "\t// disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE is 0).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool try_enqueue(T&& item)\n" +
            "\t{\n" +
            "\t\tif (inner.try_enqueue(std::move(item))) {\n" +
            "\t\t\tsema->signal();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by copying it) using an explicit producer token.\n" +
            "\t// Does not allocate memory. Fails if not enough room to enqueue.\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool try_enqueue(producer_token_t const& token, T const& item)\n" +
            "\t{\n" +
            "\t\tif (inner.try_enqueue(token, item)) {\n" +
            "\t\t\tsema->signal();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by moving it, if possible) using an explicit producer token.\n" +
            "\t// Does not allocate memory. Fails if not enough room to enqueue.\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool try_enqueue(producer_token_t const& token, T&& item)\n" +
            "\t{\n" +
            "\t\tif (inner.try_enqueue(token, std::move(item))) {\n" +
            "\t\t\tsema->signal();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues several items.\n" +
            "\t// Does not allocate memory (except for one-time implicit producer).\n" +
            "\t// Fails if not enough room to enqueue (or implicit production is\n" +
            "\t// disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE is 0).\n" +
            "\t// Note: Use std::make_move_iterator if the elements should be moved\n" +
            "\t// instead of copied.\n" +
            "\t// Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline bool try_enqueue_bulk(It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\tif (inner.try_enqueue_bulk(std::forward<It>(itemFirst), count)) {\n" +
            "\t\t\tsema->signal((LightweightSemaphore::ssize_t)(ssize_t)count);\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues several items using an explicit producer token.\n" +
            "\t// Does not allocate memory. Fails if not enough room to enqueue.\n" +
            "\t// Note: Use std::make_move_iterator if the elements should be moved\n" +
            "\t// instead of copied.\n" +
            "\t// Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline bool try_enqueue_bulk(producer_token_t const& token, It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\tif (inner.try_enqueue_bulk(token, std::forward<It>(itemFirst), count)) {\n" +
            "\t\t\tsema->signal((LightweightSemaphore::ssize_t)(ssize_t)count);\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t// Attempts to dequeue from the queue.\n" +
            "\t// Returns false if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tinline bool try_dequeue(U& item)\n" +
            "\t{\n" +
            "\t\tif (sema->tryWait()) {\n" +
            "\t\t\twhile (!inner.try_dequeue(item)) {\n" +
            "\t\t\t\tcontinue;\n" +
            "\t\t\t}\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue from the queue using an explicit consumer token.\n" +
            "\t// Returns false if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tinline bool try_dequeue(consumer_token_t& token, U& item)\n" +
            "\t{\n" +
            "\t\tif (sema->tryWait()) {\n" +
            "\t\t\twhile (!inner.try_dequeue(token, item)) {\n" +
            "\t\t\t\tcontinue;\n" +
            "\t\t\t}\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue.\n" +
            "\t// Returns the number of items actually dequeued.\n" +
            "\t// Returns 0 if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline size_t try_dequeue_bulk(It itemFirst, size_t max)\n" +
            "\t{\n" +
            "\t\tsize_t count = 0;\n" +
            "\t\tmax = (size_t)sema->tryWaitMany((LightweightSemaphore::ssize_t)(ssize_t)max);\n" +
            "\t\twhile (count != max) {\n" +
            "\t\t\tcount += inner.template try_dequeue_bulk<It&>(itemFirst, max - count);\n" +
            "\t\t}\n" +
            "\t\treturn count;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue using an explicit consumer token.\n" +
            "\t// Returns the number of items actually dequeued.\n" +
            "\t// Returns 0 if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline size_t try_dequeue_bulk(consumer_token_t& token, It itemFirst, size_t max)\n" +
            "\t{\n" +
            "\t\tsize_t count = 0;\n" +
            "\t\tmax = (size_t)sema->tryWaitMany((LightweightSemaphore::ssize_t)(ssize_t)max);\n" +
            "\t\twhile (count != max) {\n" +
            "\t\t\tcount += inner.template try_dequeue_bulk<It&>(token, itemFirst, max - count);\n" +
            "\t\t}\n" +
            "\t\treturn count;\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t\n" +
            "\t// Blocks the current thread until there's something to dequeue, then\n" +
            "\t// dequeues it.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tinline void wait_dequeue(U& item)\n" +
            "\t{\n" +
            "\t\twhile (!sema->wait()) {\n" +
            "\t\t\tcontinue;\n" +
            "\t\t}\n" +
            "\t\twhile (!inner.try_dequeue(item)) {\n" +
            "\t\t\tcontinue;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\n" +
            "\t// Blocks the current thread until either there's something to dequeue\n" +
            "\t// or the timeout (specified in microseconds) expires. Returns false\n" +
            "\t// without setting `item` if the timeout expires, otherwise assigns\n" +
            "\t// to `item` and returns true.\n" +
            "\t// Using a negative timeout indicates an indefinite timeout,\n" +
            "\t// and is thus functionally equivalent to calling wait_dequeue.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tinline bool wait_dequeue_timed(U& item, std::int64_t timeout_usecs)\n" +
            "\t{\n" +
            "\t\tif (!sema->wait(timeout_usecs)) {\n" +
            "\t\t\treturn false;\n" +
            "\t\t}\n" +
            "\t\twhile (!inner.try_dequeue(item)) {\n" +
            "\t\t\tcontinue;\n" +
            "\t\t}\n" +
            "\t\treturn true;\n" +
            "\t}\n" +
            "    \n" +
            "    // Blocks the current thread until either there's something to dequeue\n" +
            "\t// or the timeout expires. Returns false without setting `item` if the\n" +
            "    // timeout expires, otherwise assigns to `item` and returns true.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U, typename Rep, typename Period>\n" +
            "\tinline bool wait_dequeue_timed(U& item, std::chrono::duration<Rep, Period> const& timeout)\n" +
            "    {\n" +
            "        return wait_dequeue_timed(item, std::chrono::duration_cast<std::chrono::microseconds>(timeout).count());\n" +
            "    }\n" +
            "\t\n" +
            "\t// Blocks the current thread until there's something to dequeue, then\n" +
            "\t// dequeues it using an explicit consumer token.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tinline void wait_dequeue(consumer_token_t& token, U& item)\n" +
            "\t{\n" +
            "\t\twhile (!sema->wait()) {\n" +
            "\t\t\tcontinue;\n" +
            "\t\t}\n" +
            "\t\twhile (!inner.try_dequeue(token, item)) {\n" +
            "\t\t\tcontinue;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Blocks the current thread until either there's something to dequeue\n" +
            "\t// or the timeout (specified in microseconds) expires. Returns false\n" +
            "\t// without setting `item` if the timeout expires, otherwise assigns\n" +
            "\t// to `item` and returns true.\n" +
            "\t// Using a negative timeout indicates an indefinite timeout,\n" +
            "\t// and is thus functionally equivalent to calling wait_dequeue.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tinline bool wait_dequeue_timed(consumer_token_t& token, U& item, std::int64_t timeout_usecs)\n" +
            "\t{\n" +
            "\t\tif (!sema->wait(timeout_usecs)) {\n" +
            "\t\t\treturn false;\n" +
            "\t\t}\n" +
            "\t\twhile (!inner.try_dequeue(token, item)) {\n" +
            "\t\t\tcontinue;\n" +
            "\t\t}\n" +
            "\t\treturn true;\n" +
            "\t}\n" +
            "    \n" +
            "    // Blocks the current thread until either there's something to dequeue\n" +
            "\t// or the timeout expires. Returns false without setting `item` if the\n" +
            "    // timeout expires, otherwise assigns to `item` and returns true.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U, typename Rep, typename Period>\n" +
            "\tinline bool wait_dequeue_timed(consumer_token_t& token, U& item, std::chrono::duration<Rep, Period> const& timeout)\n" +
            "    {\n" +
            "        return wait_dequeue_timed(token, item, std::chrono::duration_cast<std::chrono::microseconds>(timeout).count());\n" +
            "    }\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue.\n" +
            "\t// Returns the number of items actually dequeued, which will\n" +
            "\t// always be at least one (this method blocks until the queue\n" +
            "\t// is non-empty) and at most max.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline size_t wait_dequeue_bulk(It itemFirst, size_t max)\n" +
            "\t{\n" +
            "\t\tsize_t count = 0;\n" +
            "\t\tmax = (size_t)sema->waitMany((LightweightSemaphore::ssize_t)(ssize_t)max);\n" +
            "\t\twhile (count != max) {\n" +
            "\t\t\tcount += inner.template try_dequeue_bulk<It&>(itemFirst, max - count);\n" +
            "\t\t}\n" +
            "\t\treturn count;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue.\n" +
            "\t// Returns the number of items actually dequeued, which can\n" +
            "\t// be 0 if the timeout expires while waiting for elements,\n" +
            "\t// and at most max.\n" +
            "\t// Using a negative timeout indicates an indefinite timeout,\n" +
            "\t// and is thus functionally equivalent to calling wait_dequeue_bulk.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline size_t wait_dequeue_bulk_timed(It itemFirst, size_t max, std::int64_t timeout_usecs)\n" +
            "\t{\n" +
            "\t\tsize_t count = 0;\n" +
            "\t\tmax = (size_t)sema->waitMany((LightweightSemaphore::ssize_t)(ssize_t)max, timeout_usecs);\n" +
            "\t\twhile (count != max) {\n" +
            "\t\t\tcount += inner.template try_dequeue_bulk<It&>(itemFirst, max - count);\n" +
            "\t\t}\n" +
            "\t\treturn count;\n" +
            "\t}\n" +
            "    \n" +
            "    // Attempts to dequeue several elements from the queue.\n" +
            "\t// Returns the number of items actually dequeued, which can\n" +
            "\t// be 0 if the timeout expires while waiting for elements,\n" +
            "\t// and at most max.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It, typename Rep, typename Period>\n" +
            "\tinline size_t wait_dequeue_bulk_timed(It itemFirst, size_t max, std::chrono::duration<Rep, Period> const& timeout)\n" +
            "    {\n" +
            "        return wait_dequeue_bulk_timed<It&>(itemFirst, max, std::chrono::duration_cast<std::chrono::microseconds>(timeout).count());\n" +
            "    }\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue using an explicit consumer token.\n" +
            "\t// Returns the number of items actually dequeued, which will\n" +
            "\t// always be at least one (this method blocks until the queue\n" +
            "\t// is non-empty) and at most max.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline size_t wait_dequeue_bulk(consumer_token_t& token, It itemFirst, size_t max)\n" +
            "\t{\n" +
            "\t\tsize_t count = 0;\n" +
            "\t\tmax = (size_t)sema->waitMany((LightweightSemaphore::ssize_t)(ssize_t)max);\n" +
            "\t\twhile (count != max) {\n" +
            "\t\t\tcount += inner.template try_dequeue_bulk<It&>(token, itemFirst, max - count);\n" +
            "\t\t}\n" +
            "\t\treturn count;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue using an explicit consumer token.\n" +
            "\t// Returns the number of items actually dequeued, which can\n" +
            "\t// be 0 if the timeout expires while waiting for elements,\n" +
            "\t// and at most max.\n" +
            "\t// Using a negative timeout indicates an indefinite timeout,\n" +
            "\t// and is thus functionally equivalent to calling wait_dequeue_bulk.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline size_t wait_dequeue_bulk_timed(consumer_token_t& token, It itemFirst, size_t max, std::int64_t timeout_usecs)\n" +
            "\t{\n" +
            "\t\tsize_t count = 0;\n" +
            "\t\tmax = (size_t)sema->waitMany((LightweightSemaphore::ssize_t)(ssize_t)max, timeout_usecs);\n" +
            "\t\twhile (count != max) {\n" +
            "\t\t\tcount += inner.template try_dequeue_bulk<It&>(token, itemFirst, max - count);\n" +
            "\t\t}\n" +
            "\t\treturn count;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue using an explicit consumer token.\n" +
            "\t// Returns the number of items actually dequeued, which can\n" +
            "\t// be 0 if the timeout expires while waiting for elements,\n" +
            "\t// and at most max.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It, typename Rep, typename Period>\n" +
            "\tinline size_t wait_dequeue_bulk_timed(consumer_token_t& token, It itemFirst, size_t max, std::chrono::duration<Rep, Period> const& timeout)\n" +
            "    {\n" +
            "        return wait_dequeue_bulk_timed<It&>(token, itemFirst, max, std::chrono::duration_cast<std::chrono::microseconds>(timeout).count());\n" +
            "    }\n" +
            "\t\n" +
            "\t\n" +
            "\t// Returns an estimate of the total number of elements currently in the queue. This\n" +
            "\t// estimate is only accurate if the queue has completely stabilized before it is called\n" +
            "\t// (i.e. all enqueue and dequeue operations have completed and their memory effects are\n" +
            "\t// visible on the calling thread, and no further operations start while this method is\n" +
            "\t// being called).\n" +
            "\t// Thread-safe.\n" +
            "\tinline size_t size_approx() const\n" +
            "\t{\n" +
            "\t\treturn (size_t)sema->availableApprox();\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t// Returns true if the underlying atomic variables used by\n" +
            "\t// the queue are lock-free (they should be on most platforms).\n" +
            "\t// Thread-safe.\n" +
            "\tstatic constexpr bool is_lock_free()\n" +
            "\t{\n" +
            "\t\treturn ConcurrentQueue::is_lock_free();\n" +
            "\t}\n" +
            "\t\n" +
            "\n" +
            "private:\n" +
            "\ttemplate<typename U, typename A1, typename A2>\n" +
            "\tstatic inline U* create(A1&& a1, A2&& a2)\n" +
            "\t{\n" +
            "\t\tvoid* p = (Traits::malloc)(sizeof(U));\n" +
            "\t\treturn p != nullptr ? new (p) U(std::forward<A1>(a1), std::forward<A2>(a2)) : nullptr;\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<typename U>\n" +
            "\tstatic inline void destroy(U* p)\n" +
            "\t{\n" +
            "\t\tif (p != nullptr) {\n" +
            "\t\t\tp->~U();\n" +
            "\t\t}\n" +
            "\t\t(Traits::free)(p);\n" +
            "\t}\n" +
            "\t\n" +
            "private:\n" +
            "\tConcurrentQueue inner;\n" +
            "\tstd::unique_ptr<LightweightSemaphore, void (*)(LightweightSemaphore*)> sema;\n" +
            "};\n" +
            "\n" +
            "\n" +
            "template<typename T, typename Traits>\n" +
            "inline void swap(BlockingConcurrentQueue<T, Traits>& a, BlockingConcurrentQueue<T, Traits>& b) MOODYCAMEL_NOEXCEPT\n" +
            "{\n" +
            "\ta.swap(b);\n" +
            "}\n" +
            "\n" +
            "}\t// end namespace moodycamel";

    public static String concurrentqueue = "// Provides a C++11 implementation of a multi-producer, multi-consumer lock-free queue.\n" +
            "// An overview, including benchmark results, is provided here:\n" +
            "//     http://moodycamel.com/blog/2014/a-fast-general-purpose-lock-free-queue-for-c++\n" +
            "// The full design is also described in excruciating detail at:\n" +
            "//    http://moodycamel.com/blog/2014/detailed-design-of-a-lock-free-queue\n" +
            "\n" +
            "// Simplified BSD license:\n" +
            "// Copyright (c) 2013-2020, Cameron Desrochers.\n" +
            "// All rights reserved.\n" +
            "//\n" +
            "// Redistribution and use in source and binary forms, with or without modification,\n" +
            "// are permitted provided that the following conditions are met:\n" +
            "//\n" +
            "// - Redistributions of source code must retain the above copyright notice, this list of\n" +
            "// conditions and the following disclaimer.\n" +
            "// - Redistributions in binary form must reproduce the above copyright notice, this list of\n" +
            "// conditions and the following disclaimer in the documentation and/or other materials\n" +
            "// provided with the distribution.\n" +
            "//\n" +
            "// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY\n" +
            "// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\n" +
            "// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL\n" +
            "// THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,\n" +
            "// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT\n" +
            "// OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)\n" +
            "// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR\n" +
            "// TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,\n" +
            "// EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n" +
            "\n" +
            "// Also dual-licensed under the Boost Software License (see LICENSE.md)\n" +
            "\n" +
            "#pragma once\n" +
            "\n" +
            "#if defined(__GNUC__) && !defined(__INTEL_COMPILER)\n" +
            "// Disable -Wconversion warnings (spuriously triggered when Traits::size_t and\n" +
            "// Traits::index_t are set to < 32 bits, causing integer promotion, causing warnings\n" +
            "// upon assigning any computed values)\n" +
            "#pragma GCC diagnostic push\n" +
            "#pragma GCC diagnostic ignored \"-Wconversion\"\n" +
            "\n" +
            "#ifdef MCDBGQ_USE_RELACY\n" +
            "#pragma GCC diagnostic ignored \"-Wint-to-pointer-cast\"\n" +
            "#endif\n" +
            "#endif\n" +
            "\n" +
            "#if defined(_MSC_VER) && (!defined(_HAS_CXX17) || !_HAS_CXX17)\n" +
            "// VS2019 with /W4 warns about constant conditional expressions but unless /std=c++17 or higher\n" +
            "// does not support `if constexpr`, so we have no choice but to simply disable the warning\n" +
            "#pragma warning(push)\n" +
            "#pragma warning(disable: 4127)  // conditional expression is constant\n" +
            "#endif\n" +
            "\n" +
            "#if defined(__APPLE__)\n" +
            "#include \"TargetConditionals.h\"\n" +
            "#endif\n" +
            "\n" +
            "#ifdef MCDBGQ_USE_RELACY\n" +
            "#include \"relacy/relacy_std.hpp\"\n" +
            "#include \"relacy_shims.h\"\n" +
            "// We only use malloc/free anyway, and the delete macro messes up `= delete` method declarations.\n" +
            "// We'll override the default trait malloc ourselves without a macro.\n" +
            "#undef new\n" +
            "#undef delete\n" +
            "#undef malloc\n" +
            "#undef free\n" +
            "#else\n" +
            "#include <atomic>\t\t// Requires C++11. Sorry VS2010.\n" +
            "#include <cassert>\n" +
            "#endif\n" +
            "#include <cstddef>              // for max_align_t\n" +
            "#include <cstdint>\n" +
            "#include <cstdlib>\n" +
            "#include <type_traits>\n" +
            "#include <algorithm>\n" +
            "#include <utility>\n" +
            "#include <limits>\n" +
            "#include <climits>\t\t// for CHAR_BIT\n" +
            "#include <array>\n" +
            "#include <thread>\t\t// partly for __WINPTHREADS_VERSION if on MinGW-w64 w/ POSIX threading\n" +
            "\n" +
            "// Platform-specific definitions of a numeric thread ID type and an invalid value\n" +
            "namespace moodycamel { namespace details {\n" +
            "\ttemplate<typename thread_id_t> struct thread_id_converter {\n" +
            "\t\ttypedef thread_id_t thread_id_numeric_size_t;\n" +
            "\t\ttypedef thread_id_t thread_id_hash_t;\n" +
            "\t\tstatic thread_id_hash_t prehash(thread_id_t const& x) { return x; }\n" +
            "\t};\n" +
            "} }\n" +
            "#if defined(MCDBGQ_USE_RELACY)\n" +
            "namespace moodycamel { namespace details {\n" +
            "\ttypedef std::uint32_t thread_id_t;\n" +
            "\tstatic const thread_id_t invalid_thread_id  = 0xFFFFFFFFU;\n" +
            "\tstatic const thread_id_t invalid_thread_id2 = 0xFFFFFFFEU;\n" +
            "\tstatic inline thread_id_t thread_id() { return rl::thread_index(); }\n" +
            "} }\n" +
            "#elif defined(_WIN32) || defined(__WINDOWS__) || defined(__WIN32__)\n" +
            "// No sense pulling in windows.h in a header, we'll manually declare the function\n" +
            "// we use and rely on backwards-compatibility for this not to break\n" +
            "extern \"C\" __declspec(dllimport) unsigned long __stdcall GetCurrentThreadId(void);\n" +
            "namespace moodycamel { namespace details {\n" +
            "\tstatic_assert(sizeof(unsigned long) == sizeof(std::uint32_t), \"Expected size of unsigned long to be 32 bits on Windows\");\n" +
            "\ttypedef std::uint32_t thread_id_t;\n" +
            "\tstatic const thread_id_t invalid_thread_id  = 0;\t\t\t// See http://blogs.msdn.com/b/oldnewthing/archive/2004/02/23/78395.aspx\n" +
            "\tstatic const thread_id_t invalid_thread_id2 = 0xFFFFFFFFU;\t// Not technically guaranteed to be invalid, but is never used in practice. Note that all Win32 thread IDs are presently multiples of 4.\n" +
            "\tstatic inline thread_id_t thread_id() { return static_cast<thread_id_t>(::GetCurrentThreadId()); }\n" +
            "} }\n" +
            "#elif defined(__arm__) || defined(_M_ARM) || defined(__aarch64__) || (defined(__APPLE__) && TARGET_OS_IPHONE) || defined(MOODYCAMEL_NO_THREAD_LOCAL)\n" +
            "namespace moodycamel { namespace details {\n" +
            "\tstatic_assert(sizeof(std::thread::id) == 4 || sizeof(std::thread::id) == 8, \"std::thread::id is expected to be either 4 or 8 bytes\");\n" +
            "\t\n" +
            "\ttypedef std::thread::id thread_id_t;\n" +
            "\tstatic const thread_id_t invalid_thread_id;         // Default ctor creates invalid ID\n" +
            "\n" +
            "\t// Note we don't define a invalid_thread_id2 since std::thread::id doesn't have one; it's\n" +
            "\t// only used if MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED is defined anyway, which it won't\n" +
            "\t// be.\n" +
            "\tstatic inline thread_id_t thread_id() { return std::this_thread::get_id(); }\n" +
            "\n" +
            "\ttemplate<std::size_t> struct thread_id_size { };\n" +
            "\ttemplate<> struct thread_id_size<4> { typedef std::uint32_t numeric_t; };\n" +
            "\ttemplate<> struct thread_id_size<8> { typedef std::uint64_t numeric_t; };\n" +
            "\n" +
            "\ttemplate<> struct thread_id_converter<thread_id_t> {\n" +
            "\t\ttypedef thread_id_size<sizeof(thread_id_t)>::numeric_t thread_id_numeric_size_t;\n" +
            "#ifndef __APPLE__\n" +
            "\t\ttypedef std::size_t thread_id_hash_t;\n" +
            "#else\n" +
            "\t\ttypedef thread_id_numeric_size_t thread_id_hash_t;\n" +
            "#endif\n" +
            "\n" +
            "\t\tstatic thread_id_hash_t prehash(thread_id_t const& x)\n" +
            "\t\t{\n" +
            "#ifndef __APPLE__\n" +
            "\t\t\treturn std::hash<std::thread::id>()(x);\n" +
            "#else\n" +
            "\t\t\treturn *reinterpret_cast<thread_id_hash_t const*>(&x);\n" +
            "#endif\n" +
            "\t\t}\n" +
            "\t};\n" +
            "} }\n" +
            "#else\n" +
            "// Use a nice trick from this answer: http://stackoverflow.com/a/8438730/21475\n" +
            "// In order to get a numeric thread ID in a platform-independent way, we use a thread-local\n" +
            "// static variable's address as a thread identifier :-)\n" +
            "#if defined(__GNUC__) || defined(__INTEL_COMPILER)\n" +
            "#define MOODYCAMEL_THREADLOCAL __thread\n" +
            "#elif defined(_MSC_VER)\n" +
            "#define MOODYCAMEL_THREADLOCAL __declspec(thread)\n" +
            "#else\n" +
            "// Assume C++11 compliant compiler\n" +
            "#define MOODYCAMEL_THREADLOCAL thread_local\n" +
            "#endif\n" +
            "namespace moodycamel { namespace details {\n" +
            "\ttypedef std::uintptr_t thread_id_t;\n" +
            "\tstatic const thread_id_t invalid_thread_id  = 0;\t\t// Address can't be nullptr\n" +
            "\tstatic const thread_id_t invalid_thread_id2 = 1;\t\t// Member accesses off a null pointer are also generally invalid. Plus it's not aligned.\n" +
            "\tinline thread_id_t thread_id() { static MOODYCAMEL_THREADLOCAL int x; return reinterpret_cast<thread_id_t>(&x); }\n" +
            "} }\n" +
            "#endif\n" +
            "\n" +
            "// Constexpr if\n" +
            "#ifndef MOODYCAMEL_CONSTEXPR_IF\n" +
            "#if (defined(_MSC_VER) && defined(_HAS_CXX17) && _HAS_CXX17) || __cplusplus > 201402L\n" +
            "#define MOODYCAMEL_CONSTEXPR_IF if constexpr\n" +
            "#define MOODYCAMEL_MAYBE_UNUSED [[maybe_unused]]\n" +
            "#else\n" +
            "#define MOODYCAMEL_CONSTEXPR_IF if\n" +
            "#define MOODYCAMEL_MAYBE_UNUSED\n" +
            "#endif\n" +
            "#endif\n" +
            "\n" +
            "// Exceptions\n" +
            "#ifndef MOODYCAMEL_EXCEPTIONS_ENABLED\n" +
            "#if (defined(_MSC_VER) && defined(_CPPUNWIND)) || (defined(__GNUC__) && defined(__EXCEPTIONS)) || (!defined(_MSC_VER) && !defined(__GNUC__))\n" +
            "#define MOODYCAMEL_EXCEPTIONS_ENABLED\n" +
            "#endif\n" +
            "#endif\n" +
            "#ifdef MOODYCAMEL_EXCEPTIONS_ENABLED\n" +
            "#define MOODYCAMEL_TRY try\n" +
            "#define MOODYCAMEL_CATCH(...) catch(__VA_ARGS__)\n" +
            "#define MOODYCAMEL_RETHROW throw\n" +
            "#define MOODYCAMEL_THROW(expr) throw (expr)\n" +
            "#else\n" +
            "#define MOODYCAMEL_TRY MOODYCAMEL_CONSTEXPR_IF (true)\n" +
            "#define MOODYCAMEL_CATCH(...) else MOODYCAMEL_CONSTEXPR_IF (false)\n" +
            "#define MOODYCAMEL_RETHROW\n" +
            "#define MOODYCAMEL_THROW(expr)\n" +
            "#endif\n" +
            "\n" +
            "#ifndef MOODYCAMEL_NOEXCEPT\n" +
            "#if !defined(MOODYCAMEL_EXCEPTIONS_ENABLED)\n" +
            "#define MOODYCAMEL_NOEXCEPT\n" +
            "#define MOODYCAMEL_NOEXCEPT_CTOR(type, valueType, expr) true\n" +
            "#define MOODYCAMEL_NOEXCEPT_ASSIGN(type, valueType, expr) true\n" +
            "#elif defined(_MSC_VER) && defined(_NOEXCEPT) && _MSC_VER < 1800\n" +
            "// VS2012's std::is_nothrow_[move_]constructible is broken and returns true when it shouldn't :-(\n" +
            "// We have to assume *all* non-trivial constructors may throw on VS2012!\n" +
            "#define MOODYCAMEL_NOEXCEPT _NOEXCEPT\n" +
            "#define MOODYCAMEL_NOEXCEPT_CTOR(type, valueType, expr) (std::is_rvalue_reference<valueType>::value && std::is_move_constructible<type>::value ? std::is_trivially_move_constructible<type>::value : std::is_trivially_copy_constructible<type>::value)\n" +
            "#define MOODYCAMEL_NOEXCEPT_ASSIGN(type, valueType, expr) ((std::is_rvalue_reference<valueType>::value && std::is_move_assignable<type>::value ? std::is_trivially_move_assignable<type>::value || std::is_nothrow_move_assignable<type>::value : std::is_trivially_copy_assignable<type>::value || std::is_nothrow_copy_assignable<type>::value) && MOODYCAMEL_NOEXCEPT_CTOR(type, valueType, expr))\n" +
            "#elif defined(_MSC_VER) && defined(_NOEXCEPT) && _MSC_VER < 1900\n" +
            "#define MOODYCAMEL_NOEXCEPT _NOEXCEPT\n" +
            "#define MOODYCAMEL_NOEXCEPT_CTOR(type, valueType, expr) (std::is_rvalue_reference<valueType>::value && std::is_move_constructible<type>::value ? std::is_trivially_move_constructible<type>::value || std::is_nothrow_move_constructible<type>::value : std::is_trivially_copy_constructible<type>::value || std::is_nothrow_copy_constructible<type>::value)\n" +
            "#define MOODYCAMEL_NOEXCEPT_ASSIGN(type, valueType, expr) ((std::is_rvalue_reference<valueType>::value && std::is_move_assignable<type>::value ? std::is_trivially_move_assignable<type>::value || std::is_nothrow_move_assignable<type>::value : std::is_trivially_copy_assignable<type>::value || std::is_nothrow_copy_assignable<type>::value) && MOODYCAMEL_NOEXCEPT_CTOR(type, valueType, expr))\n" +
            "#else\n" +
            "#define MOODYCAMEL_NOEXCEPT noexcept\n" +
            "#define MOODYCAMEL_NOEXCEPT_CTOR(type, valueType, expr) noexcept(expr)\n" +
            "#define MOODYCAMEL_NOEXCEPT_ASSIGN(type, valueType, expr) noexcept(expr)\n" +
            "#endif\n" +
            "#endif\n" +
            "\n" +
            "#ifndef MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "#ifdef MCDBGQ_USE_RELACY\n" +
            "#define MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "#else\n" +
            "// VS2013 doesn't support `thread_local`, and MinGW-w64 w/ POSIX threading has a crippling bug: http://sourceforge.net/p/mingw-w64/bugs/445\n" +
            "// g++ <=4.7 doesn't support thread_local either.\n" +
            "// Finally, iOS/ARM doesn't have support for it either, and g++/ARM allows it to compile but it's unconfirmed to actually work\n" +
            "#if (!defined(_MSC_VER) || _MSC_VER >= 1900) && (!defined(__MINGW32__) && !defined(__MINGW64__) || !defined(__WINPTHREADS_VERSION)) && (!defined(__GNUC__) || __GNUC__ > 4 || (__GNUC__ == 4 && __GNUC_MINOR__ >= 8)) && (!defined(__APPLE__) || !TARGET_OS_IPHONE) && !defined(__arm__) && !defined(_M_ARM) && !defined(__aarch64__)\n" +
            "// Assume `thread_local` is fully supported in all other C++11 compilers/platforms\n" +
            "//#define MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED    // always disabled for now since several users report having problems with it on\n" +
            "#endif\n" +
            "#endif\n" +
            "#endif\n" +
            "\n" +
            "// VS2012 doesn't support deleted functions. \n" +
            "// In this case, we declare the function normally but don't define it. A link error will be generated if the function is called.\n" +
            "#ifndef MOODYCAMEL_DELETE_FUNCTION\n" +
            "#if defined(_MSC_VER) && _MSC_VER < 1800\n" +
            "#define MOODYCAMEL_DELETE_FUNCTION\n" +
            "#else\n" +
            "#define MOODYCAMEL_DELETE_FUNCTION = delete\n" +
            "#endif\n" +
            "#endif\n" +
            "\n" +
            "namespace moodycamel { namespace details {\n" +
            "#ifndef MOODYCAMEL_ALIGNAS\n" +
            "// VS2013 doesn't support alignas or alignof, and align() requires a constant literal\n" +
            "#if defined(_MSC_VER) && _MSC_VER <= 1800\n" +
            "#define MOODYCAMEL_ALIGNAS(alignment) __declspec(align(alignment))\n" +
            "#define MOODYCAMEL_ALIGNOF(obj) __alignof(obj)\n" +
            "#define MOODYCAMEL_ALIGNED_TYPE_LIKE(T, obj) typename details::Vs2013Aligned<std::alignment_of<obj>::value, T>::type\n" +
            "\ttemplate<int Align, typename T> struct Vs2013Aligned { };  // default, unsupported alignment\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<1, T> { typedef __declspec(align(1)) T type; };\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<2, T> { typedef __declspec(align(2)) T type; };\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<4, T> { typedef __declspec(align(4)) T type; };\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<8, T> { typedef __declspec(align(8)) T type; };\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<16, T> { typedef __declspec(align(16)) T type; };\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<32, T> { typedef __declspec(align(32)) T type; };\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<64, T> { typedef __declspec(align(64)) T type; };\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<128, T> { typedef __declspec(align(128)) T type; };\n" +
            "\ttemplate<typename T> struct Vs2013Aligned<256, T> { typedef __declspec(align(256)) T type; };\n" +
            "#else\n" +
            "\ttemplate<typename T> struct identity { typedef T type; };\n" +
            "#define MOODYCAMEL_ALIGNAS(alignment) alignas(alignment)\n" +
            "#define MOODYCAMEL_ALIGNOF(obj) alignof(obj)\n" +
            "#define MOODYCAMEL_ALIGNED_TYPE_LIKE(T, obj) alignas(alignof(obj)) typename details::identity<T>::type\n" +
            "#endif\n" +
            "#endif\n" +
            "} }\n" +
            "\n" +
            "\n" +
            "// TSAN can false report races in lock-free code.  To enable TSAN to be used from projects that use this one,\n" +
            "// we can apply per-function compile-time suppression.\n" +
            "// See https://clang.llvm.org/docs/ThreadSanitizer.html#has-feature-thread-sanitizer\n" +
            "#define MOODYCAMEL_NO_TSAN\n" +
            "#if defined(__has_feature)\n" +
            " #if __has_feature(thread_sanitizer)\n" +
            "  #undef MOODYCAMEL_NO_TSAN\n" +
            "  #define MOODYCAMEL_NO_TSAN __attribute__((no_sanitize(\"thread\")))\n" +
            " #endif // TSAN\n" +
            "#endif // TSAN\n" +
            "\n" +
            "// Compiler-specific likely/unlikely hints\n" +
            "namespace moodycamel { namespace details {\n" +
            "#if defined(__GNUC__)\n" +
            "\tstatic inline bool (likely)(bool x) { return __builtin_expect((x), true); }\n" +
            "\tstatic inline bool (unlikely)(bool x) { return __builtin_expect((x), false); }\n" +
            "#else\n" +
            "\tstatic inline bool (likely)(bool x) { return x; }\n" +
            "\tstatic inline bool (unlikely)(bool x) { return x; }\n" +
            "#endif\n" +
            "} }\n" +
            "\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "#include \"internal/concurrentqueue_internal_debug.h\"\n" +
            "#endif\n" +
            "\n" +
            "namespace moodycamel {\n" +
            "namespace details {\n" +
            "\ttemplate<typename T>\n" +
            "\tstruct const_numeric_max {\n" +
            "\t\tstatic_assert(std::is_integral<T>::value, \"const_numeric_max can only be used with integers\");\n" +
            "\t\tstatic const T value = std::numeric_limits<T>::is_signed\n" +
            "\t\t\t? (static_cast<T>(1) << (sizeof(T) * CHAR_BIT - 1)) - static_cast<T>(1)\n" +
            "\t\t\t: static_cast<T>(-1);\n" +
            "\t};\n" +
            "\n" +
            "#if defined(__GLIBCXX__)\n" +
            "\ttypedef ::max_align_t std_max_align_t;      // libstdc++ forgot to add it to std:: for a while\n" +
            "#else\n" +
            "\ttypedef std::max_align_t std_max_align_t;   // Others (e.g. MSVC) insist it can *only* be accessed via std::\n" +
            "#endif\n" +
            "\n" +
            "\t// Some platforms have incorrectly set max_align_t to a type with <8 bytes alignment even while supporting\n" +
            "\t// 8-byte aligned scalar values (*cough* 32-bit iOS). Work around this with our own union. See issue #64.\n" +
            "\ttypedef union {\n" +
            "\t\tstd_max_align_t x;\n" +
            "\t\tlong long y;\n" +
            "\t\tvoid* z;\n" +
            "\t} max_align_t;\n" +
            "}\n" +
            "\n" +
            "// Default traits for the ConcurrentQueue. To change some of the\n" +
            "// traits without re-implementing all of them, inherit from this\n" +
            "// struct and shadow the declarations you wish to be different;\n" +
            "// since the traits are used as a template type parameter, the\n" +
            "// shadowed declarations will be used where defined, and the defaults\n" +
            "// otherwise.\n" +
            "struct ConcurrentQueueDefaultTraits\n" +
            "{\n" +
            "\t// General-purpose size type. std::size_t is strongly recommended.\n" +
            "\ttypedef std::size_t size_t;\n" +
            "\t\n" +
            "\t// The type used for the enqueue and dequeue indices. Must be at least as\n" +
            "\t// large as size_t. Should be significantly larger than the number of elements\n" +
            "\t// you expect to hold at once, especially if you have a high turnover rate;\n" +
            "\t// for example, on 32-bit x86, if you expect to have over a hundred million\n" +
            "\t// elements or pump several million elements through your queue in a very\n" +
            "\t// short space of time, using a 32-bit type *may* trigger a race condition.\n" +
            "\t// A 64-bit int type is recommended in that case, and in practice will\n" +
            "\t// prevent a race condition no matter the usage of the queue. Note that\n" +
            "\t// whether the queue is lock-free with a 64-int type depends on the whether\n" +
            "\t// std::atomic<std::uint64_t> is lock-free, which is platform-specific.\n" +
            "\ttypedef std::size_t index_t;\n" +
            "\t\n" +
            "\t// Internally, all elements are enqueued and dequeued from multi-element\n" +
            "\t// blocks; this is the smallest controllable unit. If you expect few elements\n" +
            "\t// but many producers, a smaller block size should be favoured. For few producers\n" +
            "\t// and/or many elements, a larger block size is preferred. A sane default\n" +
            "\t// is provided. Must be a power of 2.\n" +
            "\tstatic const size_t BLOCK_SIZE = 32;\n" +
            "\t\n" +
            "\t// For explicit producers (i.e. when using a producer token), the block is\n" +
            "\t// checked for being empty by iterating through a list of flags, one per element.\n" +
            "\t// For large block sizes, this is too inefficient, and switching to an atomic\n" +
            "\t// counter-based approach is faster. The switch is made for block sizes strictly\n" +
            "\t// larger than this threshold.\n" +
            "\tstatic const size_t EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD = 32;\n" +
            "\t\n" +
            "\t// How many full blocks can be expected for a single explicit producer? This should\n" +
            "\t// reflect that number's maximum for optimal performance. Must be a power of 2.\n" +
            "\tstatic const size_t EXPLICIT_INITIAL_INDEX_SIZE = 32;\n" +
            "\t\n" +
            "\t// How many full blocks can be expected for a single implicit producer? This should\n" +
            "\t// reflect that number's maximum for optimal performance. Must be a power of 2.\n" +
            "\tstatic const size_t IMPLICIT_INITIAL_INDEX_SIZE = 32;\n" +
            "\t\n" +
            "\t// The initial size of the hash table mapping thread IDs to implicit producers.\n" +
            "\t// Note that the hash is resized every time it becomes half full.\n" +
            "\t// Must be a power of two, and either 0 or at least 1. If 0, implicit production\n" +
            "\t// (using the enqueue methods without an explicit producer token) is disabled.\n" +
            "\tstatic const size_t INITIAL_IMPLICIT_PRODUCER_HASH_SIZE = 32;\n" +
            "\t\n" +
            "\t// Controls the number of items that an explicit consumer (i.e. one with a token)\n" +
            "\t// must consume before it causes all consumers to rotate and move on to the next\n" +
            "\t// internal queue.\n" +
            "\tstatic const std::uint32_t EXPLICIT_CONSUMER_CONSUMPTION_QUOTA_BEFORE_ROTATE = 256;\n" +
            "\t\n" +
            "\t// The maximum number of elements (inclusive) that can be enqueued to a sub-queue.\n" +
            "\t// Enqueue operations that would cause this limit to be surpassed will fail. Note\n" +
            "\t// that this limit is enforced at the block level (for performance reasons), i.e.\n" +
            "\t// it's rounded up to the nearest block size.\n" +
            "\tstatic const size_t MAX_SUBQUEUE_SIZE = details::const_numeric_max<size_t>::value;\n" +
            "\n" +
            "\t// The number of times to spin before sleeping when waiting on a semaphore.\n" +
            "\t// Recommended values are on the order of 1000-10000 unless the number of\n" +
            "\t// consumer threads exceeds the number of idle cores (in which case try 0-100).\n" +
            "\t// Only affects instances of the BlockingConcurrentQueue.\n" +
            "\tstatic const int MAX_SEMA_SPINS = 10000;\n" +
            "\t\n" +
            "\t\n" +
            "#ifndef MCDBGQ_USE_RELACY\n" +
            "\t// Memory allocation can be customized if needed.\n" +
            "\t// malloc should return nullptr on failure, and handle alignment like std::malloc.\n" +
            "#if defined(malloc) || defined(free)\n" +
            "\t// Gah, this is 2015, stop defining macros that break standard code already!\n" +
            "\t// Work around malloc/free being special macros:\n" +
            "\tstatic inline void* WORKAROUND_malloc(size_t size) { return malloc(size); }\n" +
            "\tstatic inline void WORKAROUND_free(void* ptr) { return free(ptr); }\n" +
            "\tstatic inline void* (malloc)(size_t size) { return WORKAROUND_malloc(size); }\n" +
            "\tstatic inline void (free)(void* ptr) { return WORKAROUND_free(ptr); }\n" +
            "#else\n" +
            "\tstatic inline void* malloc(size_t size) { return std::malloc(size); }\n" +
            "\tstatic inline void free(void* ptr) { return std::free(ptr); }\n" +
            "#endif\n" +
            "#else\n" +
            "\t// Debug versions when running under the Relacy race detector (ignore\n" +
            "\t// these in user code)\n" +
            "\tstatic inline void* malloc(size_t size) { return rl::rl_malloc(size, $); }\n" +
            "\tstatic inline void free(void* ptr) { return rl::rl_free(ptr, $); }\n" +
            "#endif\n" +
            "};\n" +
            "\n" +
            "\n" +
            "// When producing or consuming many elements, the most efficient way is to:\n" +
            "//    1) Use one of the bulk-operation methods of the queue with a token\n" +
            "//    2) Failing that, use the bulk-operation methods without a token\n" +
            "//    3) Failing that, create a token and use that with the single-item methods\n" +
            "//    4) Failing that, use the single-parameter methods of the queue\n" +
            "// Having said that, don't create tokens willy-nilly -- ideally there should be\n" +
            "// a maximum of one token per thread (of each kind).\n" +
            "struct ProducerToken;\n" +
            "struct ConsumerToken;\n" +
            "\n" +
            "template<typename T, typename Traits> class ConcurrentQueue;\n" +
            "template<typename T, typename Traits> class BlockingConcurrentQueue;\n" +
            "class ConcurrentQueueTests;\n" +
            "\n" +
            "\n" +
            "namespace details\n" +
            "{\n" +
            "\tstruct ConcurrentQueueProducerTypelessBase\n" +
            "\t{\n" +
            "\t\tConcurrentQueueProducerTypelessBase* next;\n" +
            "\t\tstd::atomic<bool> inactive;\n" +
            "\t\tProducerToken* token;\n" +
            "\t\t\n" +
            "\t\tConcurrentQueueProducerTypelessBase()\n" +
            "\t\t\t: next(nullptr), inactive(false), token(nullptr)\n" +
            "\t\t{\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\t\n" +
            "\ttemplate<bool use32> struct _hash_32_or_64 {\n" +
            "\t\tstatic inline std::uint32_t hash(std::uint32_t h)\n" +
            "\t\t{\n" +
            "\t\t\t// MurmurHash3 finalizer -- see https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp\n" +
            "\t\t\t// Since the thread ID is already unique, all we really want to do is propagate that\n" +
            "\t\t\t// uniqueness evenly across all the bits, so that we can use a subset of the bits while\n" +
            "\t\t\t// reducing collisions significantly\n" +
            "\t\t\th ^= h >> 16;\n" +
            "\t\t\th *= 0x85ebca6b;\n" +
            "\t\t\th ^= h >> 13;\n" +
            "\t\t\th *= 0xc2b2ae35;\n" +
            "\t\t\treturn h ^ (h >> 16);\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\ttemplate<> struct _hash_32_or_64<1> {\n" +
            "\t\tstatic inline std::uint64_t hash(std::uint64_t h)\n" +
            "\t\t{\n" +
            "\t\t\th ^= h >> 33;\n" +
            "\t\t\th *= 0xff51afd7ed558ccd;\n" +
            "\t\t\th ^= h >> 33;\n" +
            "\t\t\th *= 0xc4ceb9fe1a85ec53;\n" +
            "\t\t\treturn h ^ (h >> 33);\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\ttemplate<std::size_t size> struct hash_32_or_64 : public _hash_32_or_64<(size > 4)> {  };\n" +
            "\t\n" +
            "\tstatic inline size_t hash_thread_id(thread_id_t id)\n" +
            "\t{\n" +
            "\t\tstatic_assert(sizeof(thread_id_t) <= 8, \"Expected a platform where thread IDs are at most 64-bit values\");\n" +
            "\t\treturn static_cast<size_t>(hash_32_or_64<sizeof(thread_id_converter<thread_id_t>::thread_id_hash_t)>::hash(\n" +
            "\t\t\tthread_id_converter<thread_id_t>::prehash(id)));\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<typename T>\n" +
            "\tstatic inline bool circular_less_than(T a, T b)\n" +
            "\t{\n" +
            "#ifdef _MSC_VER\n" +
            "#pragma warning(push)\n" +
            "#pragma warning(disable: 4554)\n" +
            "#endif\n" +
            "\t\tstatic_assert(std::is_integral<T>::value && !std::numeric_limits<T>::is_signed, \"circular_less_than is intended to be used only with unsigned integer types\");\n" +
            "\t\treturn static_cast<T>(a - b) > static_cast<T>(static_cast<T>(1) << static_cast<T>(sizeof(T) * CHAR_BIT - 1));\n" +
            "#ifdef _MSC_VER\n" +
            "#pragma warning(pop)\n" +
            "#endif\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<typename U>\n" +
            "\tstatic inline char* align_for(char* ptr)\n" +
            "\t{\n" +
            "\t\tconst std::size_t alignment = std::alignment_of<U>::value;\n" +
            "\t\treturn ptr + (alignment - (reinterpret_cast<std::uintptr_t>(ptr) % alignment)) % alignment;\n" +
            "\t}\n" +
            "\n" +
            "\ttemplate<typename T>\n" +
            "\tstatic inline T ceil_to_pow_2(T x)\n" +
            "\t{\n" +
            "\t\tstatic_assert(std::is_integral<T>::value && !std::numeric_limits<T>::is_signed, \"ceil_to_pow_2 is intended to be used only with unsigned integer types\");\n" +
            "\n" +
            "\t\t// Adapted from http://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2\n" +
            "\t\t--x;\n" +
            "\t\tx |= x >> 1;\n" +
            "\t\tx |= x >> 2;\n" +
            "\t\tx |= x >> 4;\n" +
            "\t\tfor (std::size_t i = 1; i < sizeof(T); i <<= 1) {\n" +
            "\t\t\tx |= x >> (i << 3);\n" +
            "\t\t}\n" +
            "\t\t++x;\n" +
            "\t\treturn x;\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<typename T>\n" +
            "\tstatic inline void swap_relaxed(std::atomic<T>& left, std::atomic<T>& right)\n" +
            "\t{\n" +
            "\t\tT temp = std::move(left.load(std::memory_order_relaxed));\n" +
            "\t\tleft.store(std::move(right.load(std::memory_order_relaxed)), std::memory_order_relaxed);\n" +
            "\t\tright.store(std::move(temp), std::memory_order_relaxed);\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<typename T>\n" +
            "\tstatic inline T const& nomove(T const& x)\n" +
            "\t{\n" +
            "\t\treturn x;\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<bool Enable>\n" +
            "\tstruct nomove_if\n" +
            "\t{\n" +
            "\t\ttemplate<typename T>\n" +
            "\t\tstatic inline T const& eval(T const& x)\n" +
            "\t\t{\n" +
            "\t\t\treturn x;\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\t\n" +
            "\ttemplate<>\n" +
            "\tstruct nomove_if<false>\n" +
            "\t{\n" +
            "\t\ttemplate<typename U>\n" +
            "\t\tstatic inline auto eval(U&& x)\n" +
            "\t\t\t-> decltype(std::forward<U>(x))\n" +
            "\t\t{\n" +
            "\t\t\treturn std::forward<U>(x);\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\t\n" +
            "\ttemplate<typename It>\n" +
            "\tstatic inline auto deref_noexcept(It& it) MOODYCAMEL_NOEXCEPT -> decltype(*it)\n" +
            "\t{\n" +
            "\t\treturn *it;\n" +
            "\t}\n" +
            "\t\n" +
            "#if defined(__clang__) || !defined(__GNUC__) || __GNUC__ > 4 || (__GNUC__ == 4 && __GNUC_MINOR__ >= 8)\n" +
            "\ttemplate<typename T> struct is_trivially_destructible : std::is_trivially_destructible<T> { };\n" +
            "#else\n" +
            "\ttemplate<typename T> struct is_trivially_destructible : std::has_trivial_destructor<T> { };\n" +
            "#endif\n" +
            "\t\n" +
            "#ifdef MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "#ifdef MCDBGQ_USE_RELACY\n" +
            "\ttypedef RelacyThreadExitListener ThreadExitListener;\n" +
            "\ttypedef RelacyThreadExitNotifier ThreadExitNotifier;\n" +
            "#else\n" +
            "\tstruct ThreadExitListener\n" +
            "\t{\n" +
            "\t\ttypedef void (*callback_t)(void*);\n" +
            "\t\tcallback_t callback;\n" +
            "\t\tvoid* userData;\n" +
            "\t\t\n" +
            "\t\tThreadExitListener* next;\t\t// reserved for use by the ThreadExitNotifier\n" +
            "\t};\n" +
            "\t\n" +
            "\t\n" +
            "\tclass ThreadExitNotifier\n" +
            "\t{\n" +
            "\tpublic:\n" +
            "\t\tstatic void subscribe(ThreadExitListener* listener)\n" +
            "\t\t{\n" +
            "\t\t\tauto& tlsInst = instance();\n" +
            "\t\t\tlistener->next = tlsInst.tail;\n" +
            "\t\t\ttlsInst.tail = listener;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tstatic void unsubscribe(ThreadExitListener* listener)\n" +
            "\t\t{\n" +
            "\t\t\tauto& tlsInst = instance();\n" +
            "\t\t\tThreadExitListener** prev = &tlsInst.tail;\n" +
            "\t\t\tfor (auto ptr = tlsInst.tail; ptr != nullptr; ptr = ptr->next) {\n" +
            "\t\t\t\tif (ptr == listener) {\n" +
            "\t\t\t\t\t*prev = ptr->next;\n" +
            "\t\t\t\t\tbreak;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tprev = &ptr->next;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\tThreadExitNotifier() : tail(nullptr) { }\n" +
            "\t\tThreadExitNotifier(ThreadExitNotifier const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\t\tThreadExitNotifier& operator=(ThreadExitNotifier const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\t\t\n" +
            "\t\t~ThreadExitNotifier()\n" +
            "\t\t{\n" +
            "\t\t\t// This thread is about to exit, let everyone know!\n" +
            "\t\t\tassert(this == &instance() && \"If this assert fails, you likely have a buggy compiler! Change the preprocessor conditions such that MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED is no longer defined.\");\n" +
            "\t\t\tfor (auto ptr = tail; ptr != nullptr; ptr = ptr->next) {\n" +
            "\t\t\t\tptr->callback(ptr->userData);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Thread-local\n" +
            "\t\tstatic inline ThreadExitNotifier& instance()\n" +
            "\t\t{\n" +
            "\t\t\tstatic thread_local ThreadExitNotifier notifier;\n" +
            "\t\t\treturn notifier;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\tThreadExitListener* tail;\n" +
            "\t};\n" +
            "#endif\n" +
            "#endif\n" +
            "\t\n" +
            "\ttemplate<typename T> struct static_is_lock_free_num { enum { value = 0 }; };\n" +
            "\ttemplate<> struct static_is_lock_free_num<signed char> { enum { value = ATOMIC_CHAR_LOCK_FREE }; };\n" +
            "\ttemplate<> struct static_is_lock_free_num<short> { enum { value = ATOMIC_SHORT_LOCK_FREE }; };\n" +
            "\ttemplate<> struct static_is_lock_free_num<int> { enum { value = ATOMIC_INT_LOCK_FREE }; };\n" +
            "\ttemplate<> struct static_is_lock_free_num<long> { enum { value = ATOMIC_LONG_LOCK_FREE }; };\n" +
            "\ttemplate<> struct static_is_lock_free_num<long long> { enum { value = ATOMIC_LLONG_LOCK_FREE }; };\n" +
            "\ttemplate<typename T> struct static_is_lock_free : static_is_lock_free_num<typename std::make_signed<T>::type> {  };\n" +
            "\ttemplate<> struct static_is_lock_free<bool> { enum { value = ATOMIC_BOOL_LOCK_FREE }; };\n" +
            "\ttemplate<typename U> struct static_is_lock_free<U*> { enum { value = ATOMIC_POINTER_LOCK_FREE }; };\n" +
            "}\n" +
            "\n" +
            "\n" +
            "struct ProducerToken\n" +
            "{\n" +
            "\ttemplate<typename T, typename Traits>\n" +
            "\texplicit ProducerToken(ConcurrentQueue<T, Traits>& queue);\n" +
            "\t\n" +
            "\ttemplate<typename T, typename Traits>\n" +
            "\texplicit ProducerToken(BlockingConcurrentQueue<T, Traits>& queue);\n" +
            "\t\n" +
            "\tProducerToken(ProducerToken&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t\t: producer(other.producer)\n" +
            "\t{\n" +
            "\t\tother.producer = nullptr;\n" +
            "\t\tif (producer != nullptr) {\n" +
            "\t\t\tproducer->token = this;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\tinline ProducerToken& operator=(ProducerToken&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t{\n" +
            "\t\tswap(other);\n" +
            "\t\treturn *this;\n" +
            "\t}\n" +
            "\t\n" +
            "\tvoid swap(ProducerToken& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t{\n" +
            "\t\tstd::swap(producer, other.producer);\n" +
            "\t\tif (producer != nullptr) {\n" +
            "\t\t\tproducer->token = this;\n" +
            "\t\t}\n" +
            "\t\tif (other.producer != nullptr) {\n" +
            "\t\t\tother.producer->token = &other;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\t// A token is always valid unless:\n" +
            "\t//     1) Memory allocation failed during construction\n" +
            "\t//     2) It was moved via the move constructor\n" +
            "\t//        (Note: assignment does a swap, leaving both potentially valid)\n" +
            "\t//     3) The associated queue was destroyed\n" +
            "\t// Note that if valid() returns true, that only indicates\n" +
            "\t// that the token is valid for use with a specific queue,\n" +
            "\t// but not which one; that's up to the user to track.\n" +
            "\tinline bool valid() const { return producer != nullptr; }\n" +
            "\t\n" +
            "\t~ProducerToken()\n" +
            "\t{\n" +
            "\t\tif (producer != nullptr) {\n" +
            "\t\t\tproducer->token = nullptr;\n" +
            "\t\t\tproducer->inactive.store(true, std::memory_order_release);\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Disable copying and assignment\n" +
            "\tProducerToken(ProducerToken const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\tProducerToken& operator=(ProducerToken const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\t\n" +
            "private:\n" +
            "\ttemplate<typename T, typename Traits> friend class ConcurrentQueue;\n" +
            "\tfriend class ConcurrentQueueTests;\n" +
            "\t\n" +
            "protected:\n" +
            "\tdetails::ConcurrentQueueProducerTypelessBase* producer;\n" +
            "};\n" +
            "\n" +
            "\n" +
            "struct ConsumerToken\n" +
            "{\n" +
            "\ttemplate<typename T, typename Traits>\n" +
            "\texplicit ConsumerToken(ConcurrentQueue<T, Traits>& q);\n" +
            "\t\n" +
            "\ttemplate<typename T, typename Traits>\n" +
            "\texplicit ConsumerToken(BlockingConcurrentQueue<T, Traits>& q);\n" +
            "\t\n" +
            "\tConsumerToken(ConsumerToken&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t\t: initialOffset(other.initialOffset), lastKnownGlobalOffset(other.lastKnownGlobalOffset), itemsConsumedFromCurrent(other.itemsConsumedFromCurrent), currentProducer(other.currentProducer), desiredProducer(other.desiredProducer)\n" +
            "\t{\n" +
            "\t}\n" +
            "\t\n" +
            "\tinline ConsumerToken& operator=(ConsumerToken&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t{\n" +
            "\t\tswap(other);\n" +
            "\t\treturn *this;\n" +
            "\t}\n" +
            "\t\n" +
            "\tvoid swap(ConsumerToken& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t{\n" +
            "\t\tstd::swap(initialOffset, other.initialOffset);\n" +
            "\t\tstd::swap(lastKnownGlobalOffset, other.lastKnownGlobalOffset);\n" +
            "\t\tstd::swap(itemsConsumedFromCurrent, other.itemsConsumedFromCurrent);\n" +
            "\t\tstd::swap(currentProducer, other.currentProducer);\n" +
            "\t\tstd::swap(desiredProducer, other.desiredProducer);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Disable copying and assignment\n" +
            "\tConsumerToken(ConsumerToken const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\tConsumerToken& operator=(ConsumerToken const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\n" +
            "private:\n" +
            "\ttemplate<typename T, typename Traits> friend class ConcurrentQueue;\n" +
            "\tfriend class ConcurrentQueueTests;\n" +
            "\t\n" +
            "private: // but shared with ConcurrentQueue\n" +
            "\tstd::uint32_t initialOffset;\n" +
            "\tstd::uint32_t lastKnownGlobalOffset;\n" +
            "\tstd::uint32_t itemsConsumedFromCurrent;\n" +
            "\tdetails::ConcurrentQueueProducerTypelessBase* currentProducer;\n" +
            "\tdetails::ConcurrentQueueProducerTypelessBase* desiredProducer;\n" +
            "};\n" +
            "\n" +
            "// Need to forward-declare this swap because it's in a namespace.\n" +
            "// See http://stackoverflow.com/questions/4492062/why-does-a-c-friend-class-need-a-forward-declaration-only-in-other-namespaces\n" +
            "template<typename T, typename Traits>\n" +
            "inline void swap(typename ConcurrentQueue<T, Traits>::ImplicitProducerKVP& a, typename ConcurrentQueue<T, Traits>::ImplicitProducerKVP& b) MOODYCAMEL_NOEXCEPT;\n" +
            "\n" +
            "\n" +
            "template<typename T, typename Traits = ConcurrentQueueDefaultTraits>\n" +
            "class ConcurrentQueue\n" +
            "{\n" +
            "public:\n" +
            "\ttypedef ::moodycamel::ProducerToken producer_token_t;\n" +
            "\ttypedef ::moodycamel::ConsumerToken consumer_token_t;\n" +
            "\t\n" +
            "\ttypedef typename Traits::index_t index_t;\n" +
            "\ttypedef typename Traits::size_t size_t;\n" +
            "\t\n" +
            "\tstatic const size_t BLOCK_SIZE = static_cast<size_t>(Traits::BLOCK_SIZE);\n" +
            "\tstatic const size_t EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD = static_cast<size_t>(Traits::EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD);\n" +
            "\tstatic const size_t EXPLICIT_INITIAL_INDEX_SIZE = static_cast<size_t>(Traits::EXPLICIT_INITIAL_INDEX_SIZE);\n" +
            "\tstatic const size_t IMPLICIT_INITIAL_INDEX_SIZE = static_cast<size_t>(Traits::IMPLICIT_INITIAL_INDEX_SIZE);\n" +
            "\tstatic const size_t INITIAL_IMPLICIT_PRODUCER_HASH_SIZE = static_cast<size_t>(Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE);\n" +
            "\tstatic const std::uint32_t EXPLICIT_CONSUMER_CONSUMPTION_QUOTA_BEFORE_ROTATE = static_cast<std::uint32_t>(Traits::EXPLICIT_CONSUMER_CONSUMPTION_QUOTA_BEFORE_ROTATE);\n" +
            "#ifdef _MSC_VER\n" +
            "#pragma warning(push)\n" +
            "#pragma warning(disable: 4307)\t\t// + integral constant overflow (that's what the ternary expression is for!)\n" +
            "#pragma warning(disable: 4309)\t\t// static_cast: Truncation of constant value\n" +
            "#endif\n" +
            "\tstatic const size_t MAX_SUBQUEUE_SIZE = (details::const_numeric_max<size_t>::value - static_cast<size_t>(Traits::MAX_SUBQUEUE_SIZE) < BLOCK_SIZE) ? details::const_numeric_max<size_t>::value : ((static_cast<size_t>(Traits::MAX_SUBQUEUE_SIZE) + (BLOCK_SIZE - 1)) / BLOCK_SIZE * BLOCK_SIZE);\n" +
            "#ifdef _MSC_VER\n" +
            "#pragma warning(pop)\n" +
            "#endif\n" +
            "\n" +
            "\tstatic_assert(!std::numeric_limits<size_t>::is_signed && std::is_integral<size_t>::value, \"Traits::size_t must be an unsigned integral type\");\n" +
            "\tstatic_assert(!std::numeric_limits<index_t>::is_signed && std::is_integral<index_t>::value, \"Traits::index_t must be an unsigned integral type\");\n" +
            "\tstatic_assert(sizeof(index_t) >= sizeof(size_t), \"Traits::index_t must be at least as wide as Traits::size_t\");\n" +
            "\tstatic_assert((BLOCK_SIZE > 1) && !(BLOCK_SIZE & (BLOCK_SIZE - 1)), \"Traits::BLOCK_SIZE must be a power of 2 (and at least 2)\");\n" +
            "\tstatic_assert((EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD > 1) && !(EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD & (EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD - 1)), \"Traits::EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD must be a power of 2 (and greater than 1)\");\n" +
            "\tstatic_assert((EXPLICIT_INITIAL_INDEX_SIZE > 1) && !(EXPLICIT_INITIAL_INDEX_SIZE & (EXPLICIT_INITIAL_INDEX_SIZE - 1)), \"Traits::EXPLICIT_INITIAL_INDEX_SIZE must be a power of 2 (and greater than 1)\");\n" +
            "\tstatic_assert((IMPLICIT_INITIAL_INDEX_SIZE > 1) && !(IMPLICIT_INITIAL_INDEX_SIZE & (IMPLICIT_INITIAL_INDEX_SIZE - 1)), \"Traits::IMPLICIT_INITIAL_INDEX_SIZE must be a power of 2 (and greater than 1)\");\n" +
            "\tstatic_assert((INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) || !(INITIAL_IMPLICIT_PRODUCER_HASH_SIZE & (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE - 1)), \"Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE must be a power of 2\");\n" +
            "\tstatic_assert(INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0 || INITIAL_IMPLICIT_PRODUCER_HASH_SIZE >= 1, \"Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE must be at least 1 (or 0 to disable implicit enqueueing)\");\n" +
            "\n" +
            "public:\n" +
            "\t// Creates a queue with at least `capacity` element slots; note that the\n" +
            "\t// actual number of elements that can be inserted without additional memory\n" +
            "\t// allocation depends on the number of producers and the block size (e.g. if\n" +
            "\t// the block size is equal to `capacity`, only a single block will be allocated\n" +
            "\t// up-front, which means only a single producer will be able to enqueue elements\n" +
            "\t// without an extra allocation -- blocks aren't shared between producers).\n" +
            "\t// This method is not thread safe -- it is up to the user to ensure that the\n" +
            "\t// queue is fully constructed before it starts being used by other threads (this\n" +
            "\t// includes making the memory effects of construction visible, possibly with a\n" +
            "\t// memory barrier).\n" +
            "\texplicit ConcurrentQueue(size_t capacity = 6 * BLOCK_SIZE)\n" +
            "\t\t: producerListTail(nullptr),\n" +
            "\t\tproducerCount(0),\n" +
            "\t\tinitialBlockPoolIndex(0),\n" +
            "\t\tnextExplicitConsumerId(0),\n" +
            "\t\tglobalExplicitConsumerOffset(0)\n" +
            "\t{\n" +
            "\t\timplicitProducerHashResizeInProgress.clear(std::memory_order_relaxed);\n" +
            "\t\tpopulate_initial_implicit_producer_hash();\n" +
            "\t\tpopulate_initial_block_list(capacity / BLOCK_SIZE + ((capacity & (BLOCK_SIZE - 1)) == 0 ? 0 : 1));\n" +
            "\t\t\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "\t\t// Track all the producers using a fully-resolved typed list for\n" +
            "\t\t// each kind; this makes it possible to debug them starting from\n" +
            "\t\t// the root queue object (otherwise wacky casts are needed that\n" +
            "\t\t// don't compile in the debugger's expression evaluator).\n" +
            "\t\texplicitProducers.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\timplicitProducers.store(nullptr, std::memory_order_relaxed);\n" +
            "#endif\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Computes the correct amount of pre-allocated blocks for you based\n" +
            "\t// on the minimum number of elements you want available at any given\n" +
            "\t// time, and the maximum concurrent number of each type of producer.\n" +
            "\tConcurrentQueue(size_t minCapacity, size_t maxExplicitProducers, size_t maxImplicitProducers)\n" +
            "\t\t: producerListTail(nullptr),\n" +
            "\t\tproducerCount(0),\n" +
            "\t\tinitialBlockPoolIndex(0),\n" +
            "\t\tnextExplicitConsumerId(0),\n" +
            "\t\tglobalExplicitConsumerOffset(0)\n" +
            "\t{\n" +
            "\t\timplicitProducerHashResizeInProgress.clear(std::memory_order_relaxed);\n" +
            "\t\tpopulate_initial_implicit_producer_hash();\n" +
            "\t\tsize_t blocks = (((minCapacity + BLOCK_SIZE - 1) / BLOCK_SIZE) - 1) * (maxExplicitProducers + 1) + 2 * (maxExplicitProducers + maxImplicitProducers);\n" +
            "\t\tpopulate_initial_block_list(blocks);\n" +
            "\t\t\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "\t\texplicitProducers.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\timplicitProducers.store(nullptr, std::memory_order_relaxed);\n" +
            "#endif\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Note: The queue should not be accessed concurrently while it's\n" +
            "\t// being deleted. It's up to the user to synchronize this.\n" +
            "\t// This method is not thread safe.\n" +
            "\t~ConcurrentQueue()\n" +
            "\t{\n" +
            "\t\t// Destroy producers\n" +
            "\t\tauto ptr = producerListTail.load(std::memory_order_relaxed);\n" +
            "\t\twhile (ptr != nullptr) {\n" +
            "\t\t\tauto next = ptr->next_prod();\n" +
            "\t\t\tif (ptr->token != nullptr) {\n" +
            "\t\t\t\tptr->token->producer = nullptr;\n" +
            "\t\t\t}\n" +
            "\t\t\tdestroy(ptr);\n" +
            "\t\t\tptr = next;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Destroy implicit producer hash tables\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE != 0) {\n" +
            "\t\t\tauto hash = implicitProducerHash.load(std::memory_order_relaxed);\n" +
            "\t\t\twhile (hash != nullptr) {\n" +
            "\t\t\t\tauto prev = hash->prev;\n" +
            "\t\t\t\tif (prev != nullptr) {\t\t// The last hash is part of this object and was not allocated dynamically\n" +
            "\t\t\t\t\tfor (size_t i = 0; i != hash->capacity; ++i) {\n" +
            "\t\t\t\t\t\thash->entries[i].~ImplicitProducerKVP();\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\thash->~ImplicitProducerHash();\n" +
            "\t\t\t\t\t(Traits::free)(hash);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\thash = prev;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Destroy global free list\n" +
            "\t\tauto block = freeList.head_unsafe();\n" +
            "\t\twhile (block != nullptr) {\n" +
            "\t\t\tauto next = block->freeListNext.load(std::memory_order_relaxed);\n" +
            "\t\t\tif (block->dynamicallyAllocated) {\n" +
            "\t\t\t\tdestroy(block);\n" +
            "\t\t\t}\n" +
            "\t\t\tblock = next;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Destroy initial free list\n" +
            "\t\tdestroy_array(initialBlockPool, initialBlockPoolSize);\n" +
            "\t}\n" +
            "\n" +
            "\t// Disable copying and copy assignment\n" +
            "\tConcurrentQueue(ConcurrentQueue const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\tConcurrentQueue& operator=(ConcurrentQueue const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\t\n" +
            "\t// Moving is supported, but note that it is *not* a thread-safe operation.\n" +
            "\t// Nobody can use the queue while it's being moved, and the memory effects\n" +
            "\t// of that move must be propagated to other threads before they can use it.\n" +
            "\t// Note: When a queue is moved, its tokens are still valid but can only be\n" +
            "\t// used with the destination queue (i.e. semantically they are moved along\n" +
            "\t// with the queue itself).\n" +
            "\tConcurrentQueue(ConcurrentQueue&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t\t: producerListTail(other.producerListTail.load(std::memory_order_relaxed)),\n" +
            "\t\tproducerCount(other.producerCount.load(std::memory_order_relaxed)),\n" +
            "\t\tinitialBlockPoolIndex(other.initialBlockPoolIndex.load(std::memory_order_relaxed)),\n" +
            "\t\tinitialBlockPool(other.initialBlockPool),\n" +
            "\t\tinitialBlockPoolSize(other.initialBlockPoolSize),\n" +
            "\t\tfreeList(std::move(other.freeList)),\n" +
            "\t\tnextExplicitConsumerId(other.nextExplicitConsumerId.load(std::memory_order_relaxed)),\n" +
            "\t\tglobalExplicitConsumerOffset(other.globalExplicitConsumerOffset.load(std::memory_order_relaxed))\n" +
            "\t{\n" +
            "\t\t// Move the other one into this, and leave the other one as an empty queue\n" +
            "\t\timplicitProducerHashResizeInProgress.clear(std::memory_order_relaxed);\n" +
            "\t\tpopulate_initial_implicit_producer_hash();\n" +
            "\t\tswap_implicit_producer_hashes(other);\n" +
            "\t\t\n" +
            "\t\tother.producerListTail.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\tother.producerCount.store(0, std::memory_order_relaxed);\n" +
            "\t\tother.nextExplicitConsumerId.store(0, std::memory_order_relaxed);\n" +
            "\t\tother.globalExplicitConsumerOffset.store(0, std::memory_order_relaxed);\n" +
            "\t\t\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "\t\texplicitProducers.store(other.explicitProducers.load(std::memory_order_relaxed), std::memory_order_relaxed);\n" +
            "\t\tother.explicitProducers.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\timplicitProducers.store(other.implicitProducers.load(std::memory_order_relaxed), std::memory_order_relaxed);\n" +
            "\t\tother.implicitProducers.store(nullptr, std::memory_order_relaxed);\n" +
            "#endif\n" +
            "\t\t\n" +
            "\t\tother.initialBlockPoolIndex.store(0, std::memory_order_relaxed);\n" +
            "\t\tother.initialBlockPoolSize = 0;\n" +
            "\t\tother.initialBlockPool = nullptr;\n" +
            "\t\t\n" +
            "\t\treown_producers();\n" +
            "\t}\n" +
            "\t\n" +
            "\tinline ConcurrentQueue& operator=(ConcurrentQueue&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t{\n" +
            "\t\treturn swap_internal(other);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Swaps this queue's state with the other's. Not thread-safe.\n" +
            "\t// Swapping two queues does not invalidate their tokens, however\n" +
            "\t// the tokens that were created for one queue must be used with\n" +
            "\t// only the swapped queue (i.e. the tokens are tied to the\n" +
            "\t// queue's movable state, not the object itself).\n" +
            "\tinline void swap(ConcurrentQueue& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t{\n" +
            "\t\tswap_internal(other);\n" +
            "\t}\n" +
            "\t\n" +
            "private:\n" +
            "\tConcurrentQueue& swap_internal(ConcurrentQueue& other)\n" +
            "\t{\n" +
            "\t\tif (this == &other) {\n" +
            "\t\t\treturn *this;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tdetails::swap_relaxed(producerListTail, other.producerListTail);\n" +
            "\t\tdetails::swap_relaxed(producerCount, other.producerCount);\n" +
            "\t\tdetails::swap_relaxed(initialBlockPoolIndex, other.initialBlockPoolIndex);\n" +
            "\t\tstd::swap(initialBlockPool, other.initialBlockPool);\n" +
            "\t\tstd::swap(initialBlockPoolSize, other.initialBlockPoolSize);\n" +
            "\t\tfreeList.swap(other.freeList);\n" +
            "\t\tdetails::swap_relaxed(nextExplicitConsumerId, other.nextExplicitConsumerId);\n" +
            "\t\tdetails::swap_relaxed(globalExplicitConsumerOffset, other.globalExplicitConsumerOffset);\n" +
            "\t\t\n" +
            "\t\tswap_implicit_producer_hashes(other);\n" +
            "\t\t\n" +
            "\t\treown_producers();\n" +
            "\t\tother.reown_producers();\n" +
            "\t\t\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "\t\tdetails::swap_relaxed(explicitProducers, other.explicitProducers);\n" +
            "\t\tdetails::swap_relaxed(implicitProducers, other.implicitProducers);\n" +
            "#endif\n" +
            "\t\t\n" +
            "\t\treturn *this;\n" +
            "\t}\n" +
            "\t\n" +
            "public:\n" +
            "\t// Enqueues a single item (by copying it).\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or implicit\n" +
            "\t// production is disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE is 0,\n" +
            "\t// or Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool enqueue(T const& item)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) return false;\n" +
            "\t\telse return inner_enqueue<CanAlloc>(item);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by moving it, if possible).\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or implicit\n" +
            "\t// production is disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE is 0,\n" +
            "\t// or Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool enqueue(T&& item)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) return false;\n" +
            "\t\telse return inner_enqueue<CanAlloc>(std::move(item));\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by copying it) using an explicit producer token.\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or\n";

            public static String concurrentqueue2 =
            "\t// Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool enqueue(producer_token_t const& token, T const& item)\n" +
            "\t{\n" +
            "\t\treturn inner_enqueue<CanAlloc>(token, item);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by moving it, if possible) using an explicit producer token.\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or\n" +
            "\t// Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool enqueue(producer_token_t const& token, T&& item)\n" +
            "\t{\n" +
            "\t\treturn inner_enqueue<CanAlloc>(token, std::move(item));\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues several items.\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails (or\n" +
            "\t// implicit production is disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE\n" +
            "\t// is 0, or Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Note: Use std::make_move_iterator if the elements should be moved instead of copied.\n" +
            "\t// Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tbool enqueue_bulk(It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) return false;\n" +
            "\t\telse return inner_enqueue_bulk<CanAlloc>(itemFirst, count);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues several items using an explicit producer token.\n" +
            "\t// Allocates memory if required. Only fails if memory allocation fails\n" +
            "\t// (or Traits::MAX_SUBQUEUE_SIZE has been defined and would be surpassed).\n" +
            "\t// Note: Use std::make_move_iterator if the elements should be moved\n" +
            "\t// instead of copied.\n" +
            "\t// Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tbool enqueue_bulk(producer_token_t const& token, It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\treturn inner_enqueue_bulk<CanAlloc>(token, itemFirst, count);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by copying it).\n" +
            "\t// Does not allocate memory. Fails if not enough room to enqueue (or implicit\n" +
            "\t// production is disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE\n" +
            "\t// is 0).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool try_enqueue(T const& item)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) return false;\n" +
            "\t\telse return inner_enqueue<CannotAlloc>(item);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by moving it, if possible).\n" +
            "\t// Does not allocate memory (except for one-time implicit producer).\n" +
            "\t// Fails if not enough room to enqueue (or implicit production is\n" +
            "\t// disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE is 0).\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool try_enqueue(T&& item)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) return false;\n" +
            "\t\telse return inner_enqueue<CannotAlloc>(std::move(item));\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by copying it) using an explicit producer token.\n" +
            "\t// Does not allocate memory. Fails if not enough room to enqueue.\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool try_enqueue(producer_token_t const& token, T const& item)\n" +
            "\t{\n" +
            "\t\treturn inner_enqueue<CannotAlloc>(token, item);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues a single item (by moving it, if possible) using an explicit producer token.\n" +
            "\t// Does not allocate memory. Fails if not enough room to enqueue.\n" +
            "\t// Thread-safe.\n" +
            "\tinline bool try_enqueue(producer_token_t const& token, T&& item)\n" +
            "\t{\n" +
            "\t\treturn inner_enqueue<CannotAlloc>(token, std::move(item));\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues several items.\n" +
            "\t// Does not allocate memory (except for one-time implicit producer).\n" +
            "\t// Fails if not enough room to enqueue (or implicit production is\n" +
            "\t// disabled because Traits::INITIAL_IMPLICIT_PRODUCER_HASH_SIZE is 0).\n" +
            "\t// Note: Use std::make_move_iterator if the elements should be moved\n" +
            "\t// instead of copied.\n" +
            "\t// Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tbool try_enqueue_bulk(It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) return false;\n" +
            "\t\telse return inner_enqueue_bulk<CannotAlloc>(itemFirst, count);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Enqueues several items using an explicit producer token.\n" +
            "\t// Does not allocate memory. Fails if not enough room to enqueue.\n" +
            "\t// Note: Use std::make_move_iterator if the elements should be moved\n" +
            "\t// instead of copied.\n" +
            "\t// Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tbool try_enqueue_bulk(producer_token_t const& token, It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\treturn inner_enqueue_bulk<CannotAlloc>(token, itemFirst, count);\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t\n" +
            "\t// Attempts to dequeue from the queue.\n" +
            "\t// Returns false if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tbool try_dequeue(U& item)\n" +
            "\t{\n" +
            "\t\t// Instead of simply trying each producer in turn (which could cause needless contention on the first\n" +
            "\t\t// producer), we score them heuristically.\n" +
            "\t\tsize_t nonEmptyCount = 0;\n" +
            "\t\tProducerBase* best = nullptr;\n" +
            "\t\tsize_t bestSize = 0;\n" +
            "\t\tfor (auto ptr = producerListTail.load(std::memory_order_acquire); nonEmptyCount < 3 && ptr != nullptr; ptr = ptr->next_prod()) {\n" +
            "\t\t\tauto size = ptr->size_approx();\n" +
            "\t\t\tif (size > 0) {\n" +
            "\t\t\t\tif (size > bestSize) {\n" +
            "\t\t\t\t\tbestSize = size;\n" +
            "\t\t\t\t\tbest = ptr;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t++nonEmptyCount;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// If there was at least one non-empty queue but it appears empty at the time\n" +
            "\t\t// we try to dequeue from it, we need to make sure every queue's been tried\n" +
            "\t\tif (nonEmptyCount > 0) {\n" +
            "\t\t\tif ((details::likely)(best->dequeue(item))) {\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t\t}\n" +
            "\t\t\tfor (auto ptr = producerListTail.load(std::memory_order_acquire); ptr != nullptr; ptr = ptr->next_prod()) {\n" +
            "\t\t\t\tif (ptr != best && ptr->dequeue(item)) {\n" +
            "\t\t\t\t\treturn true;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue from the queue.\n" +
            "\t// Returns false if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// This differs from the try_dequeue(item) method in that this one does\n" +
            "\t// not attempt to reduce contention by interleaving the order that producer\n" +
            "\t// streams are dequeued from. So, using this method can reduce overall throughput\n" +
            "\t// under contention, but will give more predictable results in single-threaded\n" +
            "\t// consumer scenarios. This is mostly only useful for internal unit tests.\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tbool try_dequeue_non_interleaved(U& item)\n" +
            "\t{\n" +
            "\t\tfor (auto ptr = producerListTail.load(std::memory_order_acquire); ptr != nullptr; ptr = ptr->next_prod()) {\n" +
            "\t\t\tif (ptr->dequeue(item)) {\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue from the queue using an explicit consumer token.\n" +
            "\t// Returns false if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tbool try_dequeue(consumer_token_t& token, U& item)\n" +
            "\t{\n" +
            "\t\t// The idea is roughly as follows:\n" +
            "\t\t// Every 256 items from one producer, make everyone rotate (increase the global offset) -> this means the highest efficiency consumer dictates the rotation speed of everyone else, more or less\n" +
            "\t\t// If you see that the global offset has changed, you must reset your consumption counter and move to your designated place\n" +
            "\t\t// If there's no items where you're supposed to be, keep moving until you find a producer with some items\n" +
            "\t\t// If the global offset has not changed but you've run out of items to consume, move over from your current position until you find an producer with something in it\n" +
            "\t\t\n" +
            "\t\tif (token.desiredProducer == nullptr || token.lastKnownGlobalOffset != globalExplicitConsumerOffset.load(std::memory_order_relaxed)) {\n" +
            "\t\t\tif (!update_current_producer_after_rotation(token)) {\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// If there was at least one non-empty queue but it appears empty at the time\n" +
            "\t\t// we try to dequeue from it, we need to make sure every queue's been tried\n" +
            "\t\tif (static_cast<ProducerBase*>(token.currentProducer)->dequeue(item)) {\n" +
            "\t\t\tif (++token.itemsConsumedFromCurrent == EXPLICIT_CONSUMER_CONSUMPTION_QUOTA_BEFORE_ROTATE) {\n" +
            "\t\t\t\tglobalExplicitConsumerOffset.fetch_add(1, std::memory_order_relaxed);\n" +
            "\t\t\t}\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tauto tail = producerListTail.load(std::memory_order_acquire);\n" +
            "\t\tauto ptr = static_cast<ProducerBase*>(token.currentProducer)->next_prod();\n" +
            "\t\tif (ptr == nullptr) {\n" +
            "\t\t\tptr = tail;\n" +
            "\t\t}\n" +
            "\t\twhile (ptr != static_cast<ProducerBase*>(token.currentProducer)) {\n" +
            "\t\t\tif (ptr->dequeue(item)) {\n" +
            "\t\t\t\ttoken.currentProducer = ptr;\n" +
            "\t\t\t\ttoken.itemsConsumedFromCurrent = 1;\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t\t}\n" +
            "\t\t\tptr = ptr->next_prod();\n" +
            "\t\t\tif (ptr == nullptr) {\n" +
            "\t\t\t\tptr = tail;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue.\n" +
            "\t// Returns the number of items actually dequeued.\n" +
            "\t// Returns 0 if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tsize_t try_dequeue_bulk(It itemFirst, size_t max)\n" +
            "\t{\n" +
            "\t\tsize_t count = 0;\n" +
            "\t\tfor (auto ptr = producerListTail.load(std::memory_order_acquire); ptr != nullptr; ptr = ptr->next_prod()) {\n" +
            "\t\t\tcount += ptr->dequeue_bulk(itemFirst, max - count);\n" +
            "\t\t\tif (count == max) {\n" +
            "\t\t\t\tbreak;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\treturn count;\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from the queue using an explicit consumer token.\n" +
            "\t// Returns the number of items actually dequeued.\n" +
            "\t// Returns 0 if all producer streams appeared empty at the time they\n" +
            "\t// were checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tsize_t try_dequeue_bulk(consumer_token_t& token, It itemFirst, size_t max)\n" +
            "\t{\n" +
            "\t\tif (token.desiredProducer == nullptr || token.lastKnownGlobalOffset != globalExplicitConsumerOffset.load(std::memory_order_relaxed)) {\n" +
            "\t\t\tif (!update_current_producer_after_rotation(token)) {\n" +
            "\t\t\t\treturn 0;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tsize_t count = static_cast<ProducerBase*>(token.currentProducer)->dequeue_bulk(itemFirst, max);\n" +
            "\t\tif (count == max) {\n" +
            "\t\t\tif ((token.itemsConsumedFromCurrent += static_cast<std::uint32_t>(max)) >= EXPLICIT_CONSUMER_CONSUMPTION_QUOTA_BEFORE_ROTATE) {\n" +
            "\t\t\t\tglobalExplicitConsumerOffset.fetch_add(1, std::memory_order_relaxed);\n" +
            "\t\t\t}\n" +
            "\t\t\treturn max;\n" +
            "\t\t}\n" +
            "\t\ttoken.itemsConsumedFromCurrent += static_cast<std::uint32_t>(count);\n" +
            "\t\tmax -= count;\n" +
            "\t\t\n" +
            "\t\tauto tail = producerListTail.load(std::memory_order_acquire);\n" +
            "\t\tauto ptr = static_cast<ProducerBase*>(token.currentProducer)->next_prod();\n" +
            "\t\tif (ptr == nullptr) {\n" +
            "\t\t\tptr = tail;\n" +
            "\t\t}\n" +
            "\t\twhile (ptr != static_cast<ProducerBase*>(token.currentProducer)) {\n" +
            "\t\t\tauto dequeued = ptr->dequeue_bulk(itemFirst, max);\n" +
            "\t\t\tcount += dequeued;\n" +
            "\t\t\tif (dequeued != 0) {\n" +
            "\t\t\t\ttoken.currentProducer = ptr;\n" +
            "\t\t\t\ttoken.itemsConsumedFromCurrent = static_cast<std::uint32_t>(dequeued);\n" +
            "\t\t\t}\n" +
            "\t\t\tif (dequeued == max) {\n" +
            "\t\t\t\tbreak;\n" +
            "\t\t\t}\n" +
            "\t\t\tmax -= dequeued;\n" +
            "\t\t\tptr = ptr->next_prod();\n" +
            "\t\t\tif (ptr == nullptr) {\n" +
            "\t\t\t\tptr = tail;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\treturn count;\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t\n" +
            "\t// Attempts to dequeue from a specific producer's inner queue.\n" +
            "\t// If you happen to know which producer you want to dequeue from, this\n" +
            "\t// is significantly faster than using the general-case try_dequeue methods.\n" +
            "\t// Returns false if the producer's queue appeared empty at the time it\n" +
            "\t// was checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename U>\n" +
            "\tinline bool try_dequeue_from_producer(producer_token_t const& producer, U& item)\n" +
            "\t{\n" +
            "\t\treturn static_cast<ExplicitProducer*>(producer.producer)->dequeue(item);\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Attempts to dequeue several elements from a specific producer's inner queue.\n" +
            "\t// Returns the number of items actually dequeued.\n" +
            "\t// If you happen to know which producer you want to dequeue from, this\n" +
            "\t// is significantly faster than using the general-case try_dequeue methods.\n" +
            "\t// Returns 0 if the producer's queue appeared empty at the time it\n" +
            "\t// was checked (so, the queue is likely but not guaranteed to be empty).\n" +
            "\t// Never allocates. Thread-safe.\n" +
            "\ttemplate<typename It>\n" +
            "\tinline size_t try_dequeue_bulk_from_producer(producer_token_t const& producer, It itemFirst, size_t max)\n" +
            "\t{\n" +
            "\t\treturn static_cast<ExplicitProducer*>(producer.producer)->dequeue_bulk(itemFirst, max);\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t// Returns an estimate of the total number of elements currently in the queue. This\n" +
            "\t// estimate is only accurate if the queue has completely stabilized before it is called\n" +
            "\t// (i.e. all enqueue and dequeue operations have completed and their memory effects are\n" +
            "\t// visible on the calling thread, and no further operations start while this method is\n" +
            "\t// being called).\n" +
            "\t// Thread-safe.\n" +
            "\tsize_t size_approx() const\n" +
            "\t{\n" +
            "\t\tsize_t size = 0;\n" +
            "\t\tfor (auto ptr = producerListTail.load(std::memory_order_acquire); ptr != nullptr; ptr = ptr->next_prod()) {\n" +
            "\t\t\tsize += ptr->size_approx();\n" +
            "\t\t}\n" +
            "\t\treturn size;\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t// Returns true if the underlying atomic variables used by\n" +
            "\t// the queue are lock-free (they should be on most platforms).\n" +
            "\t// Thread-safe.\n" +
            "\tstatic constexpr bool is_lock_free()\n" +
            "\t{\n" +
            "\t\treturn\n" +
            "\t\t\tdetails::static_is_lock_free<bool>::value == 2 &&\n" +
            "\t\t\tdetails::static_is_lock_free<size_t>::value == 2 &&\n" +
            "\t\t\tdetails::static_is_lock_free<std::uint32_t>::value == 2 &&\n" +
            "\t\t\tdetails::static_is_lock_free<index_t>::value == 2 &&\n" +
            "\t\t\tdetails::static_is_lock_free<void*>::value == 2 &&\n" +
            "\t\t\tdetails::static_is_lock_free<typename details::thread_id_converter<details::thread_id_t>::thread_id_numeric_size_t>::value == 2;\n" +
            "\t}\n" +
            "\n" +
            "\n" +
            "private:\n" +
            "\tfriend struct ProducerToken;\n" +
            "\tfriend struct ConsumerToken;\n" +
            "\tstruct ExplicitProducer;\n" +
            "\tfriend struct ExplicitProducer;\n" +
            "\tstruct ImplicitProducer;\n" +
            "\tfriend struct ImplicitProducer;\n" +
            "\tfriend class ConcurrentQueueTests;\n" +
            "\t\t\n" +
            "\tenum AllocationMode { CanAlloc, CannotAlloc };\n" +
            "\t\n" +
            "\t\n" +
            "\t///////////////////////////////\n" +
            "\t// Queue methods\n" +
            "\t///////////////////////////////\n" +
            "\t\n" +
            "\ttemplate<AllocationMode canAlloc, typename U>\n" +
            "\tinline bool inner_enqueue(producer_token_t const& token, U&& element)\n" +
            "\t{\n" +
            "\t\treturn static_cast<ExplicitProducer*>(token.producer)->ConcurrentQueue::ExplicitProducer::template enqueue<canAlloc>(std::forward<U>(element));\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<AllocationMode canAlloc, typename U>\n" +
            "\tinline bool inner_enqueue(U&& element)\n" +
            "\t{\n" +
            "\t\tauto producer = get_or_add_implicit_producer();\n" +
            "\t\treturn producer == nullptr ? false : producer->ConcurrentQueue::ImplicitProducer::template enqueue<canAlloc>(std::forward<U>(element));\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<AllocationMode canAlloc, typename It>\n" +
            "\tinline bool inner_enqueue_bulk(producer_token_t const& token, It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\treturn static_cast<ExplicitProducer*>(token.producer)->ConcurrentQueue::ExplicitProducer::template enqueue_bulk<canAlloc>(itemFirst, count);\n" +
            "\t}\n" +
            "\t\n" +
            "\ttemplate<AllocationMode canAlloc, typename It>\n" +
            "\tinline bool inner_enqueue_bulk(It itemFirst, size_t count)\n" +
            "\t{\n" +
            "\t\tauto producer = get_or_add_implicit_producer();\n" +
            "\t\treturn producer == nullptr ? false : producer->ConcurrentQueue::ImplicitProducer::template enqueue_bulk<canAlloc>(itemFirst, count);\n" +
            "\t}\n" +
            "\t\n" +
            "\tinline bool update_current_producer_after_rotation(consumer_token_t& token)\n" +
            "\t{\n" +
            "\t\t// Ah, there's been a rotation, figure out where we should be!\n" +
            "\t\tauto tail = producerListTail.load(std::memory_order_acquire);\n" +
            "\t\tif (token.desiredProducer == nullptr && tail == nullptr) {\n" +
            "\t\t\treturn false;\n" +
            "\t\t}\n" +
            "\t\tauto prodCount = producerCount.load(std::memory_order_relaxed);\n" +
            "\t\tauto globalOffset = globalExplicitConsumerOffset.load(std::memory_order_relaxed);\n" +
            "\t\tif ((details::unlikely)(token.desiredProducer == nullptr)) {\n" +
            "\t\t\t// Aha, first time we're dequeueing anything.\n" +
            "\t\t\t// Figure out our local position\n" +
            "\t\t\t// Note: offset is from start, not end, but we're traversing from end -- subtract from count first\n" +
            "\t\t\tstd::uint32_t offset = prodCount - 1 - (token.initialOffset % prodCount);\n" +
            "\t\t\ttoken.desiredProducer = tail;\n" +
            "\t\t\tfor (std::uint32_t i = 0; i != offset; ++i) {\n" +
            "\t\t\t\ttoken.desiredProducer = static_cast<ProducerBase*>(token.desiredProducer)->next_prod();\n" +
            "\t\t\t\tif (token.desiredProducer == nullptr) {\n" +
            "\t\t\t\t\ttoken.desiredProducer = tail;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tstd::uint32_t delta = globalOffset - token.lastKnownGlobalOffset;\n" +
            "\t\tif (delta >= prodCount) {\n" +
            "\t\t\tdelta = delta % prodCount;\n" +
            "\t\t}\n" +
            "\t\tfor (std::uint32_t i = 0; i != delta; ++i) {\n" +
            "\t\t\ttoken.desiredProducer = static_cast<ProducerBase*>(token.desiredProducer)->next_prod();\n" +
            "\t\t\tif (token.desiredProducer == nullptr) {\n" +
            "\t\t\t\ttoken.desiredProducer = tail;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttoken.lastKnownGlobalOffset = globalOffset;\n" +
            "\t\ttoken.currentProducer = token.desiredProducer;\n" +
            "\t\ttoken.itemsConsumedFromCurrent = 0;\n" +
            "\t\treturn true;\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t///////////////////////////\n" +
            "\t// Free list\n" +
            "\t///////////////////////////\n" +
            "\t\n" +
            "\ttemplate <typename N>\n" +
            "\tstruct FreeListNode\n" +
            "\t{\n" +
            "\t\tFreeListNode() : freeListRefs(0), freeListNext(nullptr) { }\n" +
            "\t\t\n" +
            "\t\tstd::atomic<std::uint32_t> freeListRefs;\n" +
            "\t\tstd::atomic<N*> freeListNext;\n" +
            "\t};\n" +
            "\t\n" +
            "\t// A simple CAS-based lock-free free list. Not the fastest thing in the world under heavy contention, but\n" +
            "\t// simple and correct (assuming nodes are never freed until after the free list is destroyed), and fairly\n" +
            "\t// speedy under low contention.\n" +
            "\ttemplate<typename N>\t\t// N must inherit FreeListNode or have the same fields (and initialization of them)\n" +
            "\tstruct FreeList\n" +
            "\t{\n" +
            "\t\tFreeList() : freeListHead(nullptr) { }\n" +
            "\t\tFreeList(FreeList&& other) : freeListHead(other.freeListHead.load(std::memory_order_relaxed)) { other.freeListHead.store(nullptr, std::memory_order_relaxed); }\n" +
            "\t\tvoid swap(FreeList& other) { details::swap_relaxed(freeListHead, other.freeListHead); }\n" +
            "\t\t\n" +
            "\t\tFreeList(FreeList const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\t\tFreeList& operator=(FreeList const&) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\t\t\n" +
            "\t\tinline void add(N* node)\n" +
            "\t\t{\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_FREELIST\n" +
            "\t\t\tdebug::DebugLock lock(mutex);\n" +
            "#endif\t\t\n" +
            "\t\t\t// We know that the should-be-on-freelist bit is 0 at this point, so it's safe to\n" +
            "\t\t\t// set it using a fetch_add\n" +
            "\t\t\tif (node->freeListRefs.fetch_add(SHOULD_BE_ON_FREELIST, std::memory_order_acq_rel) == 0) {\n" +
            "\t\t\t\t// Oh look! We were the last ones referencing this node, and we know\n" +
            "\t\t\t\t// we want to add it to the free list, so let's do it!\n" +
            "\t\t \t\tadd_knowing_refcount_is_zero(node);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline N* try_get()\n" +
            "\t\t{\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_FREELIST\n" +
            "\t\t\tdebug::DebugLock lock(mutex);\n" +
            "#endif\t\t\n" +
            "\t\t\tauto head = freeListHead.load(std::memory_order_acquire);\n" +
            "\t\t\twhile (head != nullptr) {\n" +
            "\t\t\t\tauto prevHead = head;\n" +
            "\t\t\t\tauto refs = head->freeListRefs.load(std::memory_order_relaxed);\n" +
            "\t\t\t\tif ((refs & REFS_MASK) == 0 || !head->freeListRefs.compare_exchange_strong(refs, refs + 1, std::memory_order_acquire, std::memory_order_relaxed)) {\n" +
            "\t\t\t\t\thead = freeListHead.load(std::memory_order_acquire);\n" +
            "\t\t\t\t\tcontinue;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Good, reference count has been incremented (it wasn't at zero), which means we can read the\n" +
            "\t\t\t\t// next and not worry about it changing between now and the time we do the CAS\n" +
            "\t\t\t\tauto next = head->freeListNext.load(std::memory_order_relaxed);\n" +
            "\t\t\t\tif (freeListHead.compare_exchange_strong(head, next, std::memory_order_acquire, std::memory_order_relaxed)) {\n" +
            "\t\t\t\t\t// Yay, got the node. This means it was on the list, which means shouldBeOnFreeList must be false no\n" +
            "\t\t\t\t\t// matter the refcount (because nobody else knows it's been taken off yet, it can't have been put back on).\n" +
            "\t\t\t\t\tassert((head->freeListRefs.load(std::memory_order_relaxed) & SHOULD_BE_ON_FREELIST) == 0);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Decrease refcount twice, once for our ref, and once for the list's ref\n" +
            "\t\t\t\t\thead->freeListRefs.fetch_sub(2, std::memory_order_release);\n" +
            "\t\t\t\t\treturn head;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// OK, the head must have changed on us, but we still need to decrease the refcount we increased.\n" +
            "\t\t\t\t// Note that we don't need to release any memory effects, but we do need to ensure that the reference\n" +
            "\t\t\t\t// count decrement happens-after the CAS on the head.\n" +
            "\t\t\t\trefs = prevHead->freeListRefs.fetch_sub(1, std::memory_order_acq_rel);\n" +
            "\t\t\t\tif (refs == SHOULD_BE_ON_FREELIST + 1) {\n" +
            "\t\t\t\t\tadd_knowing_refcount_is_zero(prevHead);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\treturn nullptr;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Useful for traversing the list when there's no contention (e.g. to destroy remaining nodes)\n" +
            "\t\tN* head_unsafe() const { return freeListHead.load(std::memory_order_relaxed); }\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\tinline void add_knowing_refcount_is_zero(N* node)\n" +
            "\t\t{\n" +
            "\t\t\t// Since the refcount is zero, and nobody can increase it once it's zero (except us, and we run\n" +
            "\t\t\t// only one copy of this method per node at a time, i.e. the single thread case), then we know\n" +
            "\t\t\t// we can safely change the next pointer of the node; however, once the refcount is back above\n" +
            "\t\t\t// zero, then other threads could increase it (happens under heavy contention, when the refcount\n" +
            "\t\t\t// goes to zero in between a load and a refcount increment of a node in try_get, then back up to\n" +
            "\t\t\t// something non-zero, then the refcount increment is done by the other thread) -- so, if the CAS\n" +
            "\t\t\t// to add the node to the actual list fails, decrease the refcount and leave the add operation to\n" +
            "\t\t\t// the next thread who puts the refcount back at zero (which could be us, hence the loop).\n" +
            "\t\t\tauto head = freeListHead.load(std::memory_order_relaxed);\n" +
            "\t\t\twhile (true) {\n" +
            "\t\t\t\tnode->freeListNext.store(head, std::memory_order_relaxed);\n" +
            "\t\t\t\tnode->freeListRefs.store(1, std::memory_order_release);\n" +
            "\t\t\t\tif (!freeListHead.compare_exchange_strong(head, node, std::memory_order_release, std::memory_order_relaxed)) {\n" +
            "\t\t\t\t\t// Hmm, the add failed, but we can only try again when the refcount goes back to zero\n" +
            "\t\t\t\t\tif (node->freeListRefs.fetch_add(SHOULD_BE_ON_FREELIST - 1, std::memory_order_release) == 1) {\n" +
            "\t\t\t\t\t\tcontinue;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\treturn;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\t// Implemented like a stack, but where node order doesn't matter (nodes are inserted out of order under contention)\n" +
            "\t\tstd::atomic<N*> freeListHead;\n" +
            "\t\n" +
            "\tstatic const std::uint32_t REFS_MASK = 0x7FFFFFFF;\n" +
            "\tstatic const std::uint32_t SHOULD_BE_ON_FREELIST = 0x80000000;\n" +
            "\t\t\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_FREELIST\n" +
            "\t\tdebug::DebugMutex mutex;\n" +
            "#endif\n" +
            "\t};\n" +
            "\t\n" +
            "\t\n" +
            "\t///////////////////////////\n" +
            "\t// Block\n" +
            "\t///////////////////////////\n" +
            "\t\n" +
            "\tenum InnerQueueContext { implicit_context = 0, explicit_context = 1 };\n" +
            "\t\n" +
            "\tstruct Block\n" +
            "\t{\n" +
            "\t\tBlock()\n" +
            "\t\t\t: next(nullptr), elementsCompletelyDequeued(0), freeListRefs(0), freeListNext(nullptr), shouldBeOnFreeList(false), dynamicallyAllocated(true)\n" +
            "\t\t{\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\t\towner = nullptr;\n" +
            "#endif\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<InnerQueueContext context>\n" +
            "\t\tinline bool is_empty() const\n" +
            "\t\t{\n" +
            "\t\t\tMOODYCAMEL_CONSTEXPR_IF (context == explicit_context && BLOCK_SIZE <= EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD) {\n" +
            "\t\t\t\t// Check flags\n" +
            "\t\t\t\tfor (size_t i = 0; i < BLOCK_SIZE; ++i) {\n" +
            "\t\t\t\t\tif (!emptyFlags[i].load(std::memory_order_relaxed)) {\n" +
            "\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Aha, empty; make sure we have all other memory effects that happened before the empty flags were set\n" +
            "\t\t\t\tstd::atomic_thread_fence(std::memory_order_acquire);\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\t// Check counter\n" +
            "\t\t\t\tif (elementsCompletelyDequeued.load(std::memory_order_relaxed) == BLOCK_SIZE) {\n" +
            "\t\t\t\t\tstd::atomic_thread_fence(std::memory_order_acquire);\n" +
            "\t\t\t\t\treturn true;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tassert(elementsCompletelyDequeued.load(std::memory_order_relaxed) <= BLOCK_SIZE);\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Returns true if the block is now empty (does not apply in explicit context)\n" +
            "\t\ttemplate<InnerQueueContext context>\n" +
            "\t\tinline bool set_empty(MOODYCAMEL_MAYBE_UNUSED index_t i)\n" +
            "\t\t{\n" +
            "\t\t\tMOODYCAMEL_CONSTEXPR_IF (context == explicit_context && BLOCK_SIZE <= EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD) {\n" +
            "\t\t\t\t// Set flag\n" +
            "\t\t\t\tassert(!emptyFlags[BLOCK_SIZE - 1 - static_cast<size_t>(i & static_cast<index_t>(BLOCK_SIZE - 1))].load(std::memory_order_relaxed));\n" +
            "\t\t\t\temptyFlags[BLOCK_SIZE - 1 - static_cast<size_t>(i & static_cast<index_t>(BLOCK_SIZE - 1))].store(true, std::memory_order_release);\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\t// Increment counter\n" +
            "\t\t\t\tauto prevVal = elementsCompletelyDequeued.fetch_add(1, std::memory_order_release);\n" +
            "\t\t\t\tassert(prevVal < BLOCK_SIZE);\n" +
            "\t\t\t\treturn prevVal == BLOCK_SIZE - 1;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Sets multiple contiguous item statuses to 'empty' (assumes no wrapping and count > 0).\n" +
            "\t\t// Returns true if the block is now empty (does not apply in explicit context).\n" +
            "\t\ttemplate<InnerQueueContext context>\n" +
            "\t\tinline bool set_many_empty(MOODYCAMEL_MAYBE_UNUSED index_t i, size_t count)\n" +
            "\t\t{\n" +
            "\t\t\tMOODYCAMEL_CONSTEXPR_IF (context == explicit_context && BLOCK_SIZE <= EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD) {\n" +
            "\t\t\t\t// Set flags\n" +
            "\t\t\t\tstd::atomic_thread_fence(std::memory_order_release);\n" +
            "\t\t\t\ti = BLOCK_SIZE - 1 - static_cast<size_t>(i & static_cast<index_t>(BLOCK_SIZE - 1)) - count + 1;\n" +
            "\t\t\t\tfor (size_t j = 0; j != count; ++j) {\n" +
            "\t\t\t\t\tassert(!emptyFlags[i + j].load(std::memory_order_relaxed));\n" +
            "\t\t\t\t\temptyFlags[i + j].store(true, std::memory_order_relaxed);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\t// Increment counter\n" +
            "\t\t\t\tauto prevVal = elementsCompletelyDequeued.fetch_add(count, std::memory_order_release);\n" +
            "\t\t\t\tassert(prevVal + count <= BLOCK_SIZE);\n" +
            "\t\t\t\treturn prevVal + count == BLOCK_SIZE;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<InnerQueueContext context>\n" +
            "\t\tinline void set_all_empty()\n" +
            "\t\t{\n" +
            "\t\t\tMOODYCAMEL_CONSTEXPR_IF (context == explicit_context && BLOCK_SIZE <= EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD) {\n" +
            "\t\t\t\t// Set all flags\n" +
            "\t\t\t\tfor (size_t i = 0; i != BLOCK_SIZE; ++i) {\n" +
            "\t\t\t\t\temptyFlags[i].store(true, std::memory_order_relaxed);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\t// Reset counter\n" +
            "\t\t\t\telementsCompletelyDequeued.store(BLOCK_SIZE, std::memory_order_relaxed);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<InnerQueueContext context>\n" +
            "\t\tinline void reset_empty()\n" +
            "\t\t{\n" +
            "\t\t\tMOODYCAMEL_CONSTEXPR_IF (context == explicit_context && BLOCK_SIZE <= EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD) {\n" +
            "\t\t\t\t// Reset flags\n" +
            "\t\t\t\tfor (size_t i = 0; i != BLOCK_SIZE; ++i) {\n" +
            "\t\t\t\t\temptyFlags[i].store(false, std::memory_order_relaxed);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\t// Reset counter\n" +
            "\t\t\t\telementsCompletelyDequeued.store(0, std::memory_order_relaxed);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline T* operator[](index_t idx) MOODYCAMEL_NOEXCEPT { return static_cast<T*>(static_cast<void*>(elements)) + static_cast<size_t>(idx & static_cast<index_t>(BLOCK_SIZE - 1)); }\n" +
            "\t\tinline T const* operator[](index_t idx) const MOODYCAMEL_NOEXCEPT { return static_cast<T const*>(static_cast<void const*>(elements)) + static_cast<size_t>(idx & static_cast<index_t>(BLOCK_SIZE - 1)); }\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\tstatic_assert(std::alignment_of<T>::value <= sizeof(T), \"The queue does not support types with an alignment greater than their size at this time\");\n" +
            "\t\tMOODYCAMEL_ALIGNED_TYPE_LIKE(char[sizeof(T) * BLOCK_SIZE], T) elements;\n" +
            "\tpublic:\n" +
            "\t\tBlock* next;\n" +
            "\t\tstd::atomic<size_t> elementsCompletelyDequeued;\n" +
            "\t\tstd::atomic<bool> emptyFlags[BLOCK_SIZE <= EXPLICIT_BLOCK_EMPTY_COUNTER_THRESHOLD ? BLOCK_SIZE : 1];\n" +
            "\tpublic:\n" +
            "\t\tstd::atomic<std::uint32_t> freeListRefs;\n" +
            "\t\tstd::atomic<Block*> freeListNext;\n" +
            "\t\tstd::atomic<bool> shouldBeOnFreeList;\n" +
            "\t\tbool dynamicallyAllocated;\t\t// Perhaps a better name for this would be 'isNotPartOfInitialBlockPool'\n" +
            "\t\t\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\tvoid* owner;\n" +
            "#endif\n" +
            "\t};\n" +
            "\tstatic_assert(std::alignment_of<Block>::value >= std::alignment_of<T>::value, \"Internal error: Blocks must be at least as aligned as the type they are wrapping\");\n" +
            "\n" +
            "\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "public:\n" +
            "\tstruct MemStats;\n" +
            "private:\n" +
            "#endif\n" +
            "\t\n" +
            "\t///////////////////////////\n" +
            "\t// Producer base\n" +
            "\t///////////////////////////\n" +
            "\t\n" +
            "\tstruct ProducerBase : public details::ConcurrentQueueProducerTypelessBase\n" +
            "\t{\n" +
            "\t\tProducerBase(ConcurrentQueue* parent_, bool isExplicit_) :\n" +
            "\t\t\ttailIndex(0),\n" +
            "\t\t\theadIndex(0),\n" +
            "\t\t\tdequeueOptimisticCount(0),\n" +
            "\t\t\tdequeueOvercommit(0),\n" +
            "\t\t\ttailBlock(nullptr),\n" +
            "\t\t\tisExplicit(isExplicit_),\n" +
            "\t\t\tparent(parent_)\n" +
            "\t\t{\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tvirtual ~ProducerBase() { }\n" +
            "\t\t\n" +
            "\t\ttemplate<typename U>\n" +
            "\t\tinline bool dequeue(U& element)\n" +
            "\t\t{\n" +
            "\t\t\tif (isExplicit) {\n" +
            "\t\t\t\treturn static_cast<ExplicitProducer*>(this)->dequeue(element);\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\treturn static_cast<ImplicitProducer*>(this)->dequeue(element);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<typename It>\n" +
            "\t\tinline size_t dequeue_bulk(It& itemFirst, size_t max)\n" +
            "\t\t{\n" +
            "\t\t\tif (isExplicit) {\n" +
            "\t\t\t\treturn static_cast<ExplicitProducer*>(this)->dequeue_bulk(itemFirst, max);\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\treturn static_cast<ImplicitProducer*>(this)->dequeue_bulk(itemFirst, max);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline ProducerBase* next_prod() const { return static_cast<ProducerBase*>(next); }\n" +
            "\t\t\n" +
            "\t\tinline size_t size_approx() const\n" +
            "\t\t{\n" +
            "\t\t\tauto tail = tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto head = headIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\treturn details::circular_less_than(head, tail) ? static_cast<size_t>(tail - head) : 0;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline index_t getTail() const { return tailIndex.load(std::memory_order_relaxed); }\n" +
            "\tprotected:\n" +
            "\t\tstd::atomic<index_t> tailIndex;\t\t// Where to enqueue to next\n" +
            "\t\tstd::atomic<index_t> headIndex;\t\t// Where to dequeue from next\n" +
            "\t\t\n" +
            "\t\tstd::atomic<index_t> dequeueOptimisticCount;\n" +
            "\t\tstd::atomic<index_t> dequeueOvercommit;\n" +
            "\t\t\n" +
            "\t\tBlock* tailBlock;\n" +
            "\t\t\n" +
            "\tpublic:\n" +
            "\t\tbool isExplicit;\n" +
            "\t\tConcurrentQueue* parent;\n" +
            "\t\t\n" +
            "\tprotected:\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\tfriend struct MemStats;\n" +
            "#endif\n" +
            "\t};\n" +
            "\t\n" +
            "\t\n" +
            "\t///////////////////////////\n" +
            "\t// Explicit queue\n" +
            "\t///////////////////////////\n" +
            "\t\t\n" +
            "\tstruct ExplicitProducer : public ProducerBase\n" +
            "\t{\n" +
            "\t\texplicit ExplicitProducer(ConcurrentQueue* parent_) :\n" +
            "\t\t\tProducerBase(parent_, true),\n" +
            "\t\t\tblockIndex(nullptr),\n" +
            "\t\t\tpr_blockIndexSlotsUsed(0),\n" +
            "\t\t\tpr_blockIndexSize(EXPLICIT_INITIAL_INDEX_SIZE >> 1),\n" +
            "\t\t\tpr_blockIndexFront(0),\n" +
            "\t\t\tpr_blockIndexEntries(nullptr),\n" +
            "\t\t\tpr_blockIndexRaw(nullptr)\n" +
            "\t\t{\n" +
            "\t\t\tsize_t poolBasedIndexSize = details::ceil_to_pow_2(parent_->initialBlockPoolSize) >> 1;\n" +
            "\t\t\tif (poolBasedIndexSize > pr_blockIndexSize) {\n" +
            "\t\t\t\tpr_blockIndexSize = poolBasedIndexSize;\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\tnew_block_index(0);\t\t// This creates an index with double the number of current entries, i.e. EXPLICIT_INITIAL_INDEX_SIZE\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t~ExplicitProducer()\n" +
            "\t\t{\n" +
            "\t\t\t// Destruct any elements not yet dequeued.\n" +
            "\t\t\t// Since we're in the destructor, we can assume all elements\n" +
            "\t\t\t// are either completely dequeued or completely not (no halfways).\n" +
            "\t\t\tif (this->tailBlock != nullptr) {\t\t// Note this means there must be a block index too\n" +
            "\t\t\t\t// First find the block that's partially dequeued, if any\n" +
            "\t\t\t\tBlock* halfDequeuedBlock = nullptr;\n" +
            "\t\t\t\tif ((this->headIndex.load(std::memory_order_relaxed) & static_cast<index_t>(BLOCK_SIZE - 1)) != 0) {\n" +
            "\t\t\t\t\t// The head's not on a block boundary, meaning a block somewhere is partially dequeued\n" +
            "\t\t\t\t\t// (or the head block is the tail block and was fully dequeued, but the head/tail are still not on a boundary)\n" +
            "\t\t\t\t\tsize_t i = (pr_blockIndexFront - pr_blockIndexSlotsUsed) & (pr_blockIndexSize - 1);\n" +
            "\t\t\t\t\twhile (details::circular_less_than<index_t>(pr_blockIndexEntries[i].base + BLOCK_SIZE, this->headIndex.load(std::memory_order_relaxed))) {\n" +
            "\t\t\t\t\t\ti = (i + 1) & (pr_blockIndexSize - 1);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tassert(details::circular_less_than<index_t>(pr_blockIndexEntries[i].base, this->headIndex.load(std::memory_order_relaxed)));\n" +
            "\t\t\t\t\thalfDequeuedBlock = pr_blockIndexEntries[i].block;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Start at the head block (note the first line in the loop gives us the head from the tail on the first iteration)\n" +
            "\t\t\t\tauto block = this->tailBlock;\n" +
            "\t\t\t\tdo {\n" +
            "\t\t\t\t\tblock = block->next;\n" +
            "\t\t\t\t\tif (block->ConcurrentQueue::Block::template is_empty<explicit_context>()) {\n" +
            "\t\t\t\t\t\tcontinue;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tsize_t i = 0;\t// Offset into block\n" +
            "\t\t\t\t\tif (block == halfDequeuedBlock) {\n" +
            "\t\t\t\t\t\ti = static_cast<size_t>(this->headIndex.load(std::memory_order_relaxed) & static_cast<index_t>(BLOCK_SIZE - 1));\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Walk through all the items in the block; if this is the tail block, we need to stop when we reach the tail index\n" +
            "\t\t\t\t\tauto lastValidIndex = (this->tailIndex.load(std::memory_order_relaxed) & static_cast<index_t>(BLOCK_SIZE - 1)) == 0 ? BLOCK_SIZE : static_cast<size_t>(this->tailIndex.load(std::memory_order_relaxed) & static_cast<index_t>(BLOCK_SIZE - 1));\n" +
            "\t\t\t\t\twhile (i != BLOCK_SIZE && (block != this->tailBlock || i != lastValidIndex)) {\n" +
            "\t\t\t\t\t\t(*block)[i++]->~T();\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t} while (block != this->tailBlock);\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Destroy all blocks that we own\n" +
            "\t\t\tif (this->tailBlock != nullptr) {\n" +
            "\t\t\t\tauto block = this->tailBlock;\n" +
            "\t\t\t\tdo {\n" +
            "\t\t\t\t\tauto nextBlock = block->next;\n" +
            "\t\t\t\t\tif (block->dynamicallyAllocated) {\n" +
            "\t\t\t\t\t\tdestroy(block);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\telse {\n" +
            "\t\t\t\t\t\tthis->parent->add_block_to_free_list(block);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tblock = nextBlock;\n" +
            "\t\t\t\t} while (block != this->tailBlock);\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Destroy the block indices\n" +
            "\t\t\tauto header = static_cast<BlockIndexHeader*>(pr_blockIndexRaw);\n" +
            "\t\t\twhile (header != nullptr) {\n" +
            "\t\t\t\tauto prev = static_cast<BlockIndexHeader*>(header->prev);\n" +
            "\t\t\t\theader->~BlockIndexHeader();\n" +
            "\t\t\t\t(Traits::free)(header);\n" +
            "\t\t\t\theader = prev;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<AllocationMode allocMode, typename U>\n" +
            "\t\tinline bool enqueue(U&& element)\n" +
            "\t\t{\n" +
            "\t\t\tindex_t currentTailIndex = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tindex_t newTailIndex = 1 + currentTailIndex;\n" +
            "\t\t\tif ((currentTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) == 0) {\n" +
            "\t\t\t\t// We reached the end of a block, start a new one\n" +
            "\t\t\t\tauto startBlock = this->tailBlock;\n" +
            "\t\t\t\tauto originalBlockIndexSlotsUsed = pr_blockIndexSlotsUsed;\n" +
            "\t\t\t\tif (this->tailBlock != nullptr && this->tailBlock->next->ConcurrentQueue::Block::template is_empty<explicit_context>()) {\n" +
            "\t\t\t\t\t// We can re-use the block ahead of us, it's empty!\t\t\t\t\t\n" +
            "\t\t\t\t\tthis->tailBlock = this->tailBlock->next;\n" +
            "\t\t\t\t\tthis->tailBlock->ConcurrentQueue::Block::template reset_empty<explicit_context>();\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// We'll put the block on the block index (guaranteed to be room since we're conceptually removing the\n" +
            "\t\t\t\t\t// last block from it first -- except instead of removing then adding, we can just overwrite).\n" +
            "\t\t\t\t\t// Note that there must be a valid block index here, since even if allocation failed in the ctor,\n" +
            "\t\t\t\t\t// it would have been re-attempted when adding the first block to the queue; since there is such\n" +
            "\t\t\t\t\t// a block, a block index must have been successfully allocated.\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\t// Whatever head value we see here is >= the last value we saw here (relatively),\n" +
            "\t\t\t\t\t// and <= its current value. Since we have the most recent tail, the head must be\n" +
            "\t\t\t\t\t// <= to it.\n" +
            "\t\t\t\t\tauto head = this->headIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\tassert(!details::circular_less_than<index_t>(currentTailIndex, head));\n" +
            "\t\t\t\t\tif (!details::circular_less_than<index_t>(head, currentTailIndex + BLOCK_SIZE)\n" +
            "\t\t\t\t\t\t|| (MAX_SUBQUEUE_SIZE != details::const_numeric_max<size_t>::value && (MAX_SUBQUEUE_SIZE == 0 || MAX_SUBQUEUE_SIZE - BLOCK_SIZE < currentTailIndex - head))) {\n" +
            "\t\t\t\t\t\t// We can't enqueue in another block because there's not enough leeway -- the\n" +
            "\t\t\t\t\t\t// tail could surpass the head by the time the block fills up! (Or we'll exceed\n" +
            "\t\t\t\t\t\t// the size limit, if the second part of the condition was true.)\n" +
            "\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t// We're going to need a new block; check that the block index has room\n" +
            "\t\t\t\t\tif (pr_blockIndexRaw == nullptr || pr_blockIndexSlotsUsed == pr_blockIndexSize) {\n" +
            "\t\t\t\t\t\t// Hmm, the circular block index is already full -- we'll need\n" +
            "\t\t\t\t\t\t// to allocate a new index. Note pr_blockIndexRaw can only be nullptr if\n" +
            "\t\t\t\t\t\t// the initial allocation failed in the constructor.\n" +
            "\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (allocMode == CannotAlloc) {\n" +
            "\t\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\telse if (!new_block_index(pr_blockIndexSlotsUsed)) {\n" +
            "\t\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Insert a new block in the circular linked list\n" +
            "\t\t\t\t\tauto newBlock = this->parent->ConcurrentQueue::template requisition_block<allocMode>();\n" +
            "\t\t\t\t\tif (newBlock == nullptr) {\n" +
            "\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t}\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\t\t\t\tnewBlock->owner = this;\n" +
            "#endif\n" +
            "\t\t\t\t\tnewBlock->ConcurrentQueue::Block::template reset_empty<explicit_context>();\n" +
            "\t\t\t\t\tif (this->tailBlock == nullptr) {\n" +
            "\t\t\t\t\t\tnewBlock->next = newBlock;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\telse {\n" +
            "\t\t\t\t\t\tnewBlock->next = this->tailBlock->next;\n" +
            "\t\t\t\t\t\tthis->tailBlock->next = newBlock;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tthis->tailBlock = newBlock;\n" +
            "\t\t\t\t\t++pr_blockIndexSlotsUsed;\n" +
            "\t\t\t\t}\n" +
            "\n" +
            "\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (!MOODYCAMEL_NOEXCEPT_CTOR(T, U, new (static_cast<T*>(nullptr)) T(std::forward<U>(element)))) {\n" +
            "\t\t\t\t\t// The constructor may throw. We want the element not to appear in the queue in\n" +
            "\t\t\t\t\t// that case (without corrupting the queue):\n" +
            "\t\t\t\t\tMOODYCAMEL_TRY {\n" +
            "\t\t\t\t\t\tnew ((*this->tailBlock)[currentTailIndex]) T(std::forward<U>(element));\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tMOODYCAMEL_CATCH (...) {\n" +
            "\t\t\t\t\t\t// Revert change to the current block, but leave the new block available\n" +
            "\t\t\t\t\t\t// for next time\n" +
            "\t\t\t\t\t\tpr_blockIndexSlotsUsed = originalBlockIndexSlotsUsed;\n" +
            "\t\t\t\t\t\tthis->tailBlock = startBlock == nullptr ? this->tailBlock : startBlock;\n" +
            "\t\t\t\t\t\tMOODYCAMEL_RETHROW;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\t(void)startBlock;\n" +
            "\t\t\t\t\t(void)originalBlockIndexSlotsUsed;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Add block to block index\n" +
            "\t\t\t\tauto& entry = blockIndex.load(std::memory_order_relaxed)->entries[pr_blockIndexFront];\n" +
            "\t\t\t\tentry.base = currentTailIndex;\n" +
            "\t\t\t\tentry.block = this->tailBlock;\n" +
            "\t\t\t\tblockIndex.load(std::memory_order_relaxed)->front.store(pr_blockIndexFront, std::memory_order_release);\n" +
            "\t\t\t\tpr_blockIndexFront = (pr_blockIndexFront + 1) & (pr_blockIndexSize - 1);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (!MOODYCAMEL_NOEXCEPT_CTOR(T, U, new (static_cast<T*>(nullptr)) T(std::forward<U>(element)))) {\n" +
            "\t\t\t\t\tthis->tailIndex.store(newTailIndex, std::memory_order_release);\n" +
            "\t\t\t\t\treturn true;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Enqueue\n" +
            "\t\t\tnew ((*this->tailBlock)[currentTailIndex]) T(std::forward<U>(element));\n" +
            "\t\t\t\n" +
            "\t\t\tthis->tailIndex.store(newTailIndex, std::memory_order_release);\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<typename U>\n" +
            "\t\tbool dequeue(U& element)\n" +
            "\t\t{\n" +
            "\t\t\tauto tail = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto overcommit = this->dequeueOvercommit.load(std::memory_order_relaxed);\n" +
            "\t\t\tif (details::circular_less_than<index_t>(this->dequeueOptimisticCount.load(std::memory_order_relaxed) - overcommit, tail)) {\n" +
            "\t\t\t\t// Might be something to dequeue, let's give it a try\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Note that this if is purely for performance purposes in the common case when the queue is\n" +
            "\t\t\t\t// empty and the values are eventually consistent -- we may enter here spuriously.\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Note that whatever the values of overcommit and tail are, they are not going to change (unless we\n" +
            "\t\t\t\t// change them) and must be the same value at this point (inside the if) as when the if condition was\n" +
            "\t\t\t\t// evaluated.\n" +
            "\n";

            public static String concurrentqueue3 =
            "\t\t\t\t// We insert an acquire fence here to synchronize-with the release upon incrementing dequeueOvercommit below.\n" +
            "\t\t\t\t// This ensures that whatever the value we got loaded into overcommit, the load of dequeueOptisticCount in\n" +
            "\t\t\t\t// the fetch_add below will result in a value at least as recent as that (and therefore at least as large).\n" +
            "\t\t\t\t// Note that I believe a compiler (signal) fence here would be sufficient due to the nature of fetch_add (all\n" +
            "\t\t\t\t// read-modify-write operations are guaranteed to work on the latest value in the modification order), but\n" +
            "\t\t\t\t// unfortunately that can't be shown to be correct using only the C++11 standard.\n" +
            "\t\t\t\t// See http://stackoverflow.com/questions/18223161/what-are-the-c11-memory-ordering-guarantees-in-this-corner-case\n" +
            "\t\t\t\tstd::atomic_thread_fence(std::memory_order_acquire);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Increment optimistic counter, then check if it went over the boundary\n" +
            "\t\t\t\tauto myDequeueCount = this->dequeueOptimisticCount.fetch_add(1, std::memory_order_relaxed);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Note that since dequeueOvercommit must be <= dequeueOptimisticCount (because dequeueOvercommit is only ever\n" +
            "\t\t\t\t// incremented after dequeueOptimisticCount -- this is enforced in the `else` block below), and since we now\n" +
            "\t\t\t\t// have a version of dequeueOptimisticCount that is at least as recent as overcommit (due to the release upon\n" +
            "\t\t\t\t// incrementing dequeueOvercommit and the acquire above that synchronizes with it), overcommit <= myDequeueCount.\n" +
            "\t\t\t\t// However, we can't assert this since both dequeueOptimisticCount and dequeueOvercommit may (independently)\n" +
            "\t\t\t\t// overflow; in such a case, though, the logic still holds since the difference between the two is maintained.\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Note that we reload tail here in case it changed; it will be the same value as before or greater, since\n" +
            "\t\t\t\t// this load is sequenced after (happens after) the earlier load above. This is supported by read-read\n" +
            "\t\t\t\t// coherency (as defined in the standard), explained here: http://en.cppreference.com/w/cpp/atomic/memory_order\n" +
            "\t\t\t\ttail = this->tailIndex.load(std::memory_order_acquire);\n" +
            "\t\t\t\tif ((details::likely)(details::circular_less_than<index_t>(myDequeueCount - overcommit, tail))) {\n" +
            "\t\t\t\t\t// Guaranteed to be at least one element to dequeue!\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Get the index. Note that since there's guaranteed to be at least one element, this\n" +
            "\t\t\t\t\t// will never exceed tail. We need to do an acquire-release fence here since it's possible\n" +
            "\t\t\t\t\t// that whatever condition got us to this point was for an earlier enqueued element (that\n" +
            "\t\t\t\t\t// we already see the memory effects for), but that by the time we increment somebody else\n" +
            "\t\t\t\t\t// has incremented it, and we need to see the memory effects for *that* element, which is\n" +
            "\t\t\t\t\t// in such a case is necessarily visible on the thread that incremented it in the first\n" +
            "\t\t\t\t\t// place with the more current condition (they must have acquired a tail that is at least\n" +
            "\t\t\t\t\t// as recent).\n" +
            "\t\t\t\t\tauto index = this->headIndex.fetch_add(1, std::memory_order_acq_rel);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Determine which block the element is in\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tauto localBlockIndex = blockIndex.load(std::memory_order_acquire);\n" +
            "\t\t\t\t\tauto localBlockIndexHead = localBlockIndex->front.load(std::memory_order_acquire);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// We need to be careful here about subtracting and dividing because of index wrap-around.\n" +
            "\t\t\t\t\t// When an index wraps, we need to preserve the sign of the offset when dividing it by the\n" +
            "\t\t\t\t\t// block size (in order to get a correct signed block count offset in all cases):\n" +
            "\t\t\t\t\tauto headBase = localBlockIndex->entries[localBlockIndexHead].base;\n" +
            "\t\t\t\t\tauto blockBaseIndex = index & ~static_cast<index_t>(BLOCK_SIZE - 1);\n" +
            "\t\t\t\t\tauto offset = static_cast<size_t>(static_cast<typename std::make_signed<index_t>::type>(blockBaseIndex - headBase) / BLOCK_SIZE);\n" +
            "\t\t\t\t\tauto block = localBlockIndex->entries[(localBlockIndexHead + offset) & (localBlockIndex->size - 1)].block;\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Dequeue\n" +
            "\t\t\t\t\tauto& el = *((*block)[index]);\n" +
            "\t\t\t\t\tif (!MOODYCAMEL_NOEXCEPT_ASSIGN(T, T&&, element = std::move(el))) {\n" +
            "\t\t\t\t\t\t// Make sure the element is still fully dequeued and destroyed even if the assignment\n" +
            "\t\t\t\t\t\t// throws\n" +
            "\t\t\t\t\t\tstruct Guard {\n" +
            "\t\t\t\t\t\t\tBlock* block;\n" +
            "\t\t\t\t\t\t\tindex_t index;\n" +
            "\t\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\t\t~Guard()\n" +
            "\t\t\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\t\t(*block)[index]->~T();\n" +
            "\t\t\t\t\t\t\t\tblock->ConcurrentQueue::Block::template set_empty<explicit_context>(index);\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t} guard = { block, index };\n" +
            "\n" +
            "\t\t\t\t\t\telement = std::move(el); // NOLINT\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\telse {\n" +
            "\t\t\t\t\t\telement = std::move(el); // NOLINT\n" +
            "\t\t\t\t\t\tel.~T(); // NOLINT\n" +
            "\t\t\t\t\t\tblock->ConcurrentQueue::Block::template set_empty<explicit_context>(index);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\treturn true;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\t// Wasn't anything to dequeue after all; make the effective dequeue count eventually consistent\n" +
            "\t\t\t\t\tthis->dequeueOvercommit.fetch_add(1, std::memory_order_release);\t\t// Release so that the fetch_add on dequeueOptimisticCount is guaranteed to happen before this write\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\n" +
            "\t\t\treturn false;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<AllocationMode allocMode, typename It>\n" +
            "\t\tbool MOODYCAMEL_NO_TSAN enqueue_bulk(It itemFirst, size_t count)\n" +
            "\t\t{\n" +
            "\t\t\t// First, we need to make sure we have enough room to enqueue all of the elements;\n" +
            "\t\t\t// this means pre-allocating blocks and putting them in the block index (but only if\n" +
            "\t\t\t// all the allocations succeeded).\n" +
            "\t\t\tindex_t startTailIndex = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto startBlock = this->tailBlock;\n" +
            "\t\t\tauto originalBlockIndexFront = pr_blockIndexFront;\n" +
            "\t\t\tauto originalBlockIndexSlotsUsed = pr_blockIndexSlotsUsed;\n" +
            "\t\t\t\n" +
            "\t\t\tBlock* firstAllocatedBlock = nullptr;\n" +
            "\t\t\t\n" +
            "\t\t\t// Figure out how many blocks we'll need to allocate, and do so\n" +
            "\t\t\tsize_t blockBaseDiff = ((startTailIndex + count - 1) & ~static_cast<index_t>(BLOCK_SIZE - 1)) - ((startTailIndex - 1) & ~static_cast<index_t>(BLOCK_SIZE - 1));\n" +
            "\t\t\tindex_t currentTailIndex = (startTailIndex - 1) & ~static_cast<index_t>(BLOCK_SIZE - 1);\n" +
            "\t\t\tif (blockBaseDiff > 0) {\n" +
            "\t\t\t\t// Allocate as many blocks as possible from ahead\n" +
            "\t\t\t\twhile (blockBaseDiff > 0 && this->tailBlock != nullptr && this->tailBlock->next != firstAllocatedBlock && this->tailBlock->next->ConcurrentQueue::Block::template is_empty<explicit_context>()) {\n" +
            "\t\t\t\t\tblockBaseDiff -= static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\tcurrentTailIndex += static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tthis->tailBlock = this->tailBlock->next;\n" +
            "\t\t\t\t\tfirstAllocatedBlock = firstAllocatedBlock == nullptr ? this->tailBlock : firstAllocatedBlock;\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tauto& entry = blockIndex.load(std::memory_order_relaxed)->entries[pr_blockIndexFront];\n" +
            "\t\t\t\t\tentry.base = currentTailIndex;\n" +
            "\t\t\t\t\tentry.block = this->tailBlock;\n" +
            "\t\t\t\t\tpr_blockIndexFront = (pr_blockIndexFront + 1) & (pr_blockIndexSize - 1);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Now allocate as many blocks as necessary from the block pool\n" +
            "\t\t\t\twhile (blockBaseDiff > 0) {\n" +
            "\t\t\t\t\tblockBaseDiff -= static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\tcurrentTailIndex += static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tauto head = this->headIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\tassert(!details::circular_less_than<index_t>(currentTailIndex, head));\n" +
            "\t\t\t\t\tbool full = !details::circular_less_than<index_t>(head, currentTailIndex + BLOCK_SIZE) || (MAX_SUBQUEUE_SIZE != details::const_numeric_max<size_t>::value && (MAX_SUBQUEUE_SIZE == 0 || MAX_SUBQUEUE_SIZE - BLOCK_SIZE < currentTailIndex - head));\n" +
            "\t\t\t\t\tif (pr_blockIndexRaw == nullptr || pr_blockIndexSlotsUsed == pr_blockIndexSize || full) {\n" +
            "\t\t\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (allocMode == CannotAlloc) {\n" +
            "\t\t\t\t\t\t\t// Failed to allocate, undo changes (but keep injected blocks)\n" +
            "\t\t\t\t\t\t\tpr_blockIndexFront = originalBlockIndexFront;\n" +
            "\t\t\t\t\t\t\tpr_blockIndexSlotsUsed = originalBlockIndexSlotsUsed;\n" +
            "\t\t\t\t\t\t\tthis->tailBlock = startBlock == nullptr ? firstAllocatedBlock : startBlock;\n" +
            "\t\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\telse if (full || !new_block_index(originalBlockIndexSlotsUsed)) {\n" +
            "\t\t\t\t\t\t\t// Failed to allocate, undo changes (but keep injected blocks)\n" +
            "\t\t\t\t\t\t\tpr_blockIndexFront = originalBlockIndexFront;\n" +
            "\t\t\t\t\t\t\tpr_blockIndexSlotsUsed = originalBlockIndexSlotsUsed;\n" +
            "\t\t\t\t\t\t\tthis->tailBlock = startBlock == nullptr ? firstAllocatedBlock : startBlock;\n" +
            "\t\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\t// pr_blockIndexFront is updated inside new_block_index, so we need to\n" +
            "\t\t\t\t\t\t// update our fallback value too (since we keep the new index even if we\n" +
            "\t\t\t\t\t\t// later fail)\n" +
            "\t\t\t\t\t\toriginalBlockIndexFront = originalBlockIndexSlotsUsed;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Insert a new block in the circular linked list\n" +
            "\t\t\t\t\tauto newBlock = this->parent->ConcurrentQueue::template requisition_block<allocMode>();\n" +
            "\t\t\t\t\tif (newBlock == nullptr) {\n" +
            "\t\t\t\t\t\tpr_blockIndexFront = originalBlockIndexFront;\n" +
            "\t\t\t\t\t\tpr_blockIndexSlotsUsed = originalBlockIndexSlotsUsed;\n" +
            "\t\t\t\t\t\tthis->tailBlock = startBlock == nullptr ? firstAllocatedBlock : startBlock;\n" +
            "\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\t\t\t\tnewBlock->owner = this;\n" +
            "#endif\n" +
            "\t\t\t\t\tnewBlock->ConcurrentQueue::Block::template set_all_empty<explicit_context>();\n" +
            "\t\t\t\t\tif (this->tailBlock == nullptr) {\n" +
            "\t\t\t\t\t\tnewBlock->next = newBlock;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\telse {\n" +
            "\t\t\t\t\t\tnewBlock->next = this->tailBlock->next;\n" +
            "\t\t\t\t\t\tthis->tailBlock->next = newBlock;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tthis->tailBlock = newBlock;\n" +
            "\t\t\t\t\tfirstAllocatedBlock = firstAllocatedBlock == nullptr ? this->tailBlock : firstAllocatedBlock;\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t++pr_blockIndexSlotsUsed;\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tauto& entry = blockIndex.load(std::memory_order_relaxed)->entries[pr_blockIndexFront];\n" +
            "\t\t\t\t\tentry.base = currentTailIndex;\n" +
            "\t\t\t\t\tentry.block = this->tailBlock;\n" +
            "\t\t\t\t\tpr_blockIndexFront = (pr_blockIndexFront + 1) & (pr_blockIndexSize - 1);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Excellent, all allocations succeeded. Reset each block's emptiness before we fill them up, and\n" +
            "\t\t\t\t// publish the new block index front\n" +
            "\t\t\t\tauto block = firstAllocatedBlock;\n" +
            "\t\t\t\twhile (true) {\n" +
            "\t\t\t\t\tblock->ConcurrentQueue::Block::template reset_empty<explicit_context>();\n" +
            "\t\t\t\t\tif (block == this->tailBlock) {\n" +
            "\t\t\t\t\t\tbreak;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tblock = block->next;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (MOODYCAMEL_NOEXCEPT_CTOR(T, decltype(*itemFirst), new (static_cast<T*>(nullptr)) T(details::deref_noexcept(itemFirst)))) {\n" +
            "\t\t\t\t\tblockIndex.load(std::memory_order_relaxed)->front.store((pr_blockIndexFront - 1) & (pr_blockIndexSize - 1), std::memory_order_release);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Enqueue, one block at a time\n" +
            "\t\t\tindex_t newTailIndex = startTailIndex + static_cast<index_t>(count);\n" +
            "\t\t\tcurrentTailIndex = startTailIndex;\n" +
            "\t\t\tauto endBlock = this->tailBlock;\n" +
            "\t\t\tthis->tailBlock = startBlock;\n" +
            "\t\t\tassert((startTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) != 0 || firstAllocatedBlock != nullptr || count == 0);\n" +
            "\t\t\tif ((startTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) == 0 && firstAllocatedBlock != nullptr) {\n" +
            "\t\t\t\tthis->tailBlock = firstAllocatedBlock;\n" +
            "\t\t\t}\n" +
            "\t\t\twhile (true) {\n" +
            "\t\t\t\tindex_t stopIndex = (currentTailIndex & ~static_cast<index_t>(BLOCK_SIZE - 1)) + static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\tif (details::circular_less_than<index_t>(newTailIndex, stopIndex)) {\n" +
            "\t\t\t\t\tstopIndex = newTailIndex;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (MOODYCAMEL_NOEXCEPT_CTOR(T, decltype(*itemFirst), new (static_cast<T*>(nullptr)) T(details::deref_noexcept(itemFirst)))) {\n" +
            "\t\t\t\t\twhile (currentTailIndex != stopIndex) {\n" +
            "\t\t\t\t\t\tnew ((*this->tailBlock)[currentTailIndex++]) T(*itemFirst++);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\tMOODYCAMEL_TRY {\n" +
            "\t\t\t\t\t\twhile (currentTailIndex != stopIndex) {\n" +
            "\t\t\t\t\t\t\t// Must use copy constructor even if move constructor is available\n" +
            "\t\t\t\t\t\t\t// because we may have to revert if there's an exception.\n" +
            "\t\t\t\t\t\t\t// Sorry about the horrible templated next line, but it was the only way\n" +
            "\t\t\t\t\t\t\t// to disable moving *at compile time*, which is important because a type\n" +
            "\t\t\t\t\t\t\t// may only define a (noexcept) move constructor, and so calls to the\n" +
            "\t\t\t\t\t\t\t// cctor will not compile, even if they are in an if branch that will never\n" +
            "\t\t\t\t\t\t\t// be executed\n" +
            "\t\t\t\t\t\t\tnew ((*this->tailBlock)[currentTailIndex]) T(details::nomove_if<!MOODYCAMEL_NOEXCEPT_CTOR(T, decltype(*itemFirst), new (static_cast<T*>(nullptr)) T(details::deref_noexcept(itemFirst)))>::eval(*itemFirst));\n" +
            "\t\t\t\t\t\t\t++currentTailIndex;\n" +
            "\t\t\t\t\t\t\t++itemFirst;\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tMOODYCAMEL_CATCH (...) {\n" +
            "\t\t\t\t\t\t// Oh dear, an exception's been thrown -- destroy the elements that\n" +
            "\t\t\t\t\t\t// were enqueued so far and revert the entire bulk operation (we'll keep\n" +
            "\t\t\t\t\t\t// any allocated blocks in our linked list for later, though).\n" +
            "\t\t\t\t\t\tauto constructedStopIndex = currentTailIndex;\n" +
            "\t\t\t\t\t\tauto lastBlockEnqueued = this->tailBlock;\n" +
            "\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\tpr_blockIndexFront = originalBlockIndexFront;\n" +
            "\t\t\t\t\t\tpr_blockIndexSlotsUsed = originalBlockIndexSlotsUsed;\n" +
            "\t\t\t\t\t\tthis->tailBlock = startBlock == nullptr ? firstAllocatedBlock : startBlock;\n" +
            "\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\tif (!details::is_trivially_destructible<T>::value) {\n" +
            "\t\t\t\t\t\t\tauto block = startBlock;\n" +
            "\t\t\t\t\t\t\tif ((startTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) == 0) {\n" +
            "\t\t\t\t\t\t\t\tblock = firstAllocatedBlock;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\tcurrentTailIndex = startTailIndex;\n" +
            "\t\t\t\t\t\t\twhile (true) {\n" +
            "\t\t\t\t\t\t\t\tstopIndex = (currentTailIndex & ~static_cast<index_t>(BLOCK_SIZE - 1)) + static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\t\t\tif (details::circular_less_than<index_t>(constructedStopIndex, stopIndex)) {\n" +
            "\t\t\t\t\t\t\t\t\tstopIndex = constructedStopIndex;\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\twhile (currentTailIndex != stopIndex) {\n" +
            "\t\t\t\t\t\t\t\t\t(*block)[currentTailIndex++]->~T();\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\tif (block == lastBlockEnqueued) {\n" +
            "\t\t\t\t\t\t\t\t\tbreak;\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\tblock = block->next;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tMOODYCAMEL_RETHROW;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tif (this->tailBlock == endBlock) {\n" +
            "\t\t\t\t\tassert(currentTailIndex == newTailIndex);\n" +
            "\t\t\t\t\tbreak;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tthis->tailBlock = this->tailBlock->next;\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\tMOODYCAMEL_CONSTEXPR_IF (!MOODYCAMEL_NOEXCEPT_CTOR(T, decltype(*itemFirst), new (static_cast<T*>(nullptr)) T(details::deref_noexcept(itemFirst)))) {\n" +
            "\t\t\t\tif (firstAllocatedBlock != nullptr)\n" +
            "\t\t\t\t\tblockIndex.load(std::memory_order_relaxed)->front.store((pr_blockIndexFront - 1) & (pr_blockIndexSize - 1), std::memory_order_release);\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\tthis->tailIndex.store(newTailIndex, std::memory_order_release);\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<typename It>\n" +
            "\t\tsize_t dequeue_bulk(It& itemFirst, size_t max)\n" +
            "\t\t{\n" +
            "\t\t\tauto tail = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto overcommit = this->dequeueOvercommit.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto desiredCount = static_cast<size_t>(tail - (this->dequeueOptimisticCount.load(std::memory_order_relaxed) - overcommit));\n" +
            "\t\t\tif (details::circular_less_than<size_t>(0, desiredCount)) {\n" +
            "\t\t\t\tdesiredCount = desiredCount < max ? desiredCount : max;\n" +
            "\t\t\t\tstd::atomic_thread_fence(std::memory_order_acquire);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tauto myDequeueCount = this->dequeueOptimisticCount.fetch_add(desiredCount, std::memory_order_relaxed);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\ttail = this->tailIndex.load(std::memory_order_acquire);\n" +
            "\t\t\t\tauto actualCount = static_cast<size_t>(tail - (myDequeueCount - overcommit));\n" +
            "\t\t\t\tif (details::circular_less_than<size_t>(0, actualCount)) {\n" +
            "\t\t\t\t\tactualCount = desiredCount < actualCount ? desiredCount : actualCount;\n" +
            "\t\t\t\t\tif (actualCount < desiredCount) {\n" +
            "\t\t\t\t\t\tthis->dequeueOvercommit.fetch_add(desiredCount - actualCount, std::memory_order_release);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Get the first index. Note that since there's guaranteed to be at least actualCount elements, this\n" +
            "\t\t\t\t\t// will never exceed tail.\n" +
            "\t\t\t\t\tauto firstIndex = this->headIndex.fetch_add(actualCount, std::memory_order_acq_rel);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Determine which block the first element is in\n" +
            "\t\t\t\t\tauto localBlockIndex = blockIndex.load(std::memory_order_acquire);\n" +
            "\t\t\t\t\tauto localBlockIndexHead = localBlockIndex->front.load(std::memory_order_acquire);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tauto headBase = localBlockIndex->entries[localBlockIndexHead].base;\n" +
            "\t\t\t\t\tauto firstBlockBaseIndex = firstIndex & ~static_cast<index_t>(BLOCK_SIZE - 1);\n" +
            "\t\t\t\t\tauto offset = static_cast<size_t>(static_cast<typename std::make_signed<index_t>::type>(firstBlockBaseIndex - headBase) / BLOCK_SIZE);\n" +
            "\t\t\t\t\tauto indexIndex = (localBlockIndexHead + offset) & (localBlockIndex->size - 1);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Iterate the blocks and dequeue\n" +
            "\t\t\t\t\tauto index = firstIndex;\n" +
            "\t\t\t\t\tdo {\n" +
            "\t\t\t\t\t\tauto firstIndexInBlock = index;\n" +
            "\t\t\t\t\t\tindex_t endIndex = (index & ~static_cast<index_t>(BLOCK_SIZE - 1)) + static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\tendIndex = details::circular_less_than<index_t>(firstIndex + static_cast<index_t>(actualCount), endIndex) ? firstIndex + static_cast<index_t>(actualCount) : endIndex;\n" +
            "\t\t\t\t\t\tauto block = localBlockIndex->entries[indexIndex].block;\n" +
            "\t\t\t\t\t\tif (MOODYCAMEL_NOEXCEPT_ASSIGN(T, T&&, details::deref_noexcept(itemFirst) = std::move((*(*block)[index])))) {\n" +
            "\t\t\t\t\t\t\twhile (index != endIndex) {\n" +
            "\t\t\t\t\t\t\t\tauto& el = *((*block)[index]);\n" +
            "\t\t\t\t\t\t\t\t*itemFirst++ = std::move(el);\n" +
            "\t\t\t\t\t\t\t\tel.~T();\n" +
            "\t\t\t\t\t\t\t\t++index;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\telse {\n" +
            "\t\t\t\t\t\t\tMOODYCAMEL_TRY {\n" +
            "\t\t\t\t\t\t\t\twhile (index != endIndex) {\n" +
            "\t\t\t\t\t\t\t\t\tauto& el = *((*block)[index]);\n" +
            "\t\t\t\t\t\t\t\t\t*itemFirst = std::move(el);\n" +
            "\t\t\t\t\t\t\t\t\t++itemFirst;\n" +
            "\t\t\t\t\t\t\t\t\tel.~T();\n" +
            "\t\t\t\t\t\t\t\t\t++index;\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\tMOODYCAMEL_CATCH (...) {\n" +
            "\t\t\t\t\t\t\t\t// It's too late to revert the dequeue, but we can make sure that all\n" +
            "\t\t\t\t\t\t\t\t// the dequeued objects are properly destroyed and the block index\n" +
            "\t\t\t\t\t\t\t\t// (and empty count) are properly updated before we propagate the exception\n" +
            "\t\t\t\t\t\t\t\tdo {\n" +
            "\t\t\t\t\t\t\t\t\tblock = localBlockIndex->entries[indexIndex].block;\n" +
            "\t\t\t\t\t\t\t\t\twhile (index != endIndex) {\n" +
            "\t\t\t\t\t\t\t\t\t\t(*block)[index++]->~T();\n" +
            "\t\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\t\tblock->ConcurrentQueue::Block::template set_many_empty<explicit_context>(firstIndexInBlock, static_cast<size_t>(endIndex - firstIndexInBlock));\n" +
            "\t\t\t\t\t\t\t\t\tindexIndex = (indexIndex + 1) & (localBlockIndex->size - 1);\n" +
            "\t\t\t\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\t\t\t\tfirstIndexInBlock = index;\n" +
            "\t\t\t\t\t\t\t\t\tendIndex = (index & ~static_cast<index_t>(BLOCK_SIZE - 1)) + static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\t\t\t\tendIndex = details::circular_less_than<index_t>(firstIndex + static_cast<index_t>(actualCount), endIndex) ? firstIndex + static_cast<index_t>(actualCount) : endIndex;\n" +
            "\t\t\t\t\t\t\t\t} while (index != firstIndex + actualCount);\n" +
            "\t\t\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\t\t\tMOODYCAMEL_RETHROW;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tblock->ConcurrentQueue::Block::template set_many_empty<explicit_context>(firstIndexInBlock, static_cast<size_t>(endIndex - firstIndexInBlock));\n" +
            "\t\t\t\t\t\tindexIndex = (indexIndex + 1) & (localBlockIndex->size - 1);\n" +
            "\t\t\t\t\t} while (index != firstIndex + actualCount);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\treturn actualCount;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\t// Wasn't anything to dequeue after all; make the effective dequeue count eventually consistent\n" +
            "\t\t\t\t\tthis->dequeueOvercommit.fetch_add(desiredCount, std::memory_order_release);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\treturn 0;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\tstruct BlockIndexEntry\n" +
            "\t\t{\n" +
            "\t\t\tindex_t base;\n" +
            "\t\t\tBlock* block;\n" +
            "\t\t};\n" +
            "\t\t\n" +
            "\t\tstruct BlockIndexHeader\n" +
            "\t\t{\n" +
            "\t\t\tsize_t size;\n" +
            "\t\t\tstd::atomic<size_t> front;\t\t// Current slot (not next, like pr_blockIndexFront)\n" +
            "\t\t\tBlockIndexEntry* entries;\n" +
            "\t\t\tvoid* prev;\n" +
            "\t\t};\n" +
            "\t\t\n" +
            "\t\t\n" +
            "\t\tbool new_block_index(size_t numberOfFilledSlotsToExpose)\n" +
            "\t\t{\n" +
            "\t\t\tauto prevBlockSizeMask = pr_blockIndexSize - 1;\n" +
            "\t\t\t\n" +
            "\t\t\t// Create the new block\n" +
            "\t\t\tpr_blockIndexSize <<= 1;\n" +
            "\t\t\tauto newRawPtr = static_cast<char*>((Traits::malloc)(sizeof(BlockIndexHeader) + std::alignment_of<BlockIndexEntry>::value - 1 + sizeof(BlockIndexEntry) * pr_blockIndexSize));\n" +
            "\t\t\tif (newRawPtr == nullptr) {\n" +
            "\t\t\t\tpr_blockIndexSize >>= 1;\t\t// Reset to allow graceful retry\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\tauto newBlockIndexEntries = reinterpret_cast<BlockIndexEntry*>(details::align_for<BlockIndexEntry>(newRawPtr + sizeof(BlockIndexHeader)));\n" +
            "\t\t\t\n" +
            "\t\t\t// Copy in all the old indices, if any\n" +
            "\t\t\tsize_t j = 0;\n" +
            "\t\t\tif (pr_blockIndexSlotsUsed != 0) {\n" +
            "\t\t\t\tauto i = (pr_blockIndexFront - pr_blockIndexSlotsUsed) & prevBlockSizeMask;\n" +
            "\t\t\t\tdo {\n" +
            "\t\t\t\t\tnewBlockIndexEntries[j++] = pr_blockIndexEntries[i];\n" +
            "\t\t\t\t\ti = (i + 1) & prevBlockSizeMask;\n" +
            "\t\t\t\t} while (i != pr_blockIndexFront);\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Update everything\n" +
            "\t\t\tauto header = new (newRawPtr) BlockIndexHeader;\n" +
            "\t\t\theader->size = pr_blockIndexSize;\n" +
            "\t\t\theader->front.store(numberOfFilledSlotsToExpose - 1, std::memory_order_relaxed);\n" +
            "\t\t\theader->entries = newBlockIndexEntries;\n" +
            "\t\t\theader->prev = pr_blockIndexRaw;\t\t// we link the new block to the old one so we can free it later\n" +
            "\t\t\t\n" +
            "\t\t\tpr_blockIndexFront = j;\n" +
            "\t\t\tpr_blockIndexEntries = newBlockIndexEntries;\n" +
            "\t\t\tpr_blockIndexRaw = newRawPtr;\n" +
            "\t\t\tblockIndex.store(header, std::memory_order_release);\n" +
            "\t\t\t\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\tstd::atomic<BlockIndexHeader*> blockIndex;\n" +
            "\t\t\n" +
            "\t\t// To be used by producer only -- consumer must use the ones in referenced by blockIndex\n" +
            "\t\tsize_t pr_blockIndexSlotsUsed;\n" +
            "\t\tsize_t pr_blockIndexSize;\n" +
            "\t\tsize_t pr_blockIndexFront;\t\t// Next slot (not current)\n" +
            "\t\tBlockIndexEntry* pr_blockIndexEntries;\n" +
            "\t\tvoid* pr_blockIndexRaw;\n" +
            "\t\t\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "\tpublic:\n" +
            "\t\tExplicitProducer* nextExplicitProducer;\n" +
            "\tprivate:\n" +
            "#endif\n" +
            "\t\t\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\tfriend struct MemStats;\n" +
            "#endif\n" +
            "\t};\n" +
            "\t\n" +
            "\t\n" +
            "\t//////////////////////////////////\n" +
            "\t// Implicit queue\n" +
            "\t//////////////////////////////////\n" +
            "\t\n" +
            "\tstruct ImplicitProducer : public ProducerBase\n" +
            "\t{\t\t\t\n" +
            "\t\tImplicitProducer(ConcurrentQueue* parent_) :\n" +
            "\t\t\tProducerBase(parent_, false),\n" +
            "\t\t\tnextBlockIndexCapacity(IMPLICIT_INITIAL_INDEX_SIZE),\n" +
            "\t\t\tblockIndex(nullptr)\n" +
            "\t\t{\n" +
            "\t\t\tnew_block_index();\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t~ImplicitProducer()\n" +
            "\t\t{\n" +
            "\t\t\t// Note that since we're in the destructor we can assume that all enqueue/dequeue operations\n" +
            "\t\t\t// completed already; this means that all undequeued elements are placed contiguously across\n" +
            "\t\t\t// contiguous blocks, and that only the first and last remaining blocks can be only partially\n" +
            "\t\t\t// empty (all other remaining blocks must be completely full).\n" +
            "\t\t\t\n" +
            "#ifdef MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "\t\t\t// Unregister ourselves for thread termination notification\n" +
            "\t\t\tif (!this->inactive.load(std::memory_order_relaxed)) {\n" +
            "\t\t\t\tdetails::ThreadExitNotifier::unsubscribe(&threadExitListener);\n" +
            "\t\t\t}\n" +
            "#endif\n" +
            "\t\t\t\n" +
            "\t\t\t// Destroy all remaining elements!\n" +
            "\t\t\tauto tail = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto index = this->headIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tBlock* block = nullptr;\n" +
            "\t\t\tassert(index == tail || details::circular_less_than(index, tail));\n" +
            "\t\t\tbool forceFreeLastBlock = index != tail;\t\t// If we enter the loop, then the last (tail) block will not be freed\n" +
            "\t\t\twhile (index != tail) {\n" +
            "\t\t\t\tif ((index & static_cast<index_t>(BLOCK_SIZE - 1)) == 0 || block == nullptr) {\n" +
            "\t\t\t\t\tif (block != nullptr) {\n" +
            "\t\t\t\t\t\t// Free the old block\n" +
            "\t\t\t\t\t\tthis->parent->add_block_to_free_list(block);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tblock = get_block_index_entry_for_index(index)->value.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t((*block)[index])->~T();\n" +
            "\t\t\t\t++index;\n" +
            "\t\t\t}\n" +
            "\t\t\t// Even if the queue is empty, there's still one block that's not on the free list\n" +
            "\t\t\t// (unless the head index reached the end of it, in which case the tail will be poised\n" +
            "\t\t\t// to create a new block).\n" +
            "\t\t\tif (this->tailBlock != nullptr && (forceFreeLastBlock || (tail & static_cast<index_t>(BLOCK_SIZE - 1)) != 0)) {\n" +
            "\t\t\t\tthis->parent->add_block_to_free_list(this->tailBlock);\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Destroy block index\n" +
            "\t\t\tauto localBlockIndex = blockIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tif (localBlockIndex != nullptr) {\n" +
            "\t\t\t\tfor (size_t i = 0; i != localBlockIndex->capacity; ++i) {\n" +
            "\t\t\t\t\tlocalBlockIndex->index[i]->~BlockIndexEntry();\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tdo {\n" +
            "\t\t\t\t\tauto prev = localBlockIndex->prev;\n" +
            "\t\t\t\t\tlocalBlockIndex->~BlockIndexHeader();\n" +
            "\t\t\t\t\t(Traits::free)(localBlockIndex);\n" +
            "\t\t\t\t\tlocalBlockIndex = prev;\n" +
            "\t\t\t\t} while (localBlockIndex != nullptr);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<AllocationMode allocMode, typename U>\n" +
            "\t\tinline bool enqueue(U&& element)\n" +
            "\t\t{\n" +
            "\t\t\tindex_t currentTailIndex = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tindex_t newTailIndex = 1 + currentTailIndex;\n" +
            "\t\t\tif ((currentTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) == 0) {\n" +
            "\t\t\t\t// We reached the end of a block, start a new one\n" +
            "\t\t\t\tauto head = this->headIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\tassert(!details::circular_less_than<index_t>(currentTailIndex, head));\n" +
            "\t\t\t\tif (!details::circular_less_than<index_t>(head, currentTailIndex + BLOCK_SIZE) || (MAX_SUBQUEUE_SIZE != details::const_numeric_max<size_t>::value && (MAX_SUBQUEUE_SIZE == 0 || MAX_SUBQUEUE_SIZE - BLOCK_SIZE < currentTailIndex - head))) {\n" +
            "\t\t\t\t\treturn false;\n" +
            "\t\t\t\t}\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODBLOCKINDEX\n" +
            "\t\t\t\tdebug::DebugLock lock(mutex);\n" +
            "#endif\n" +
            "\t\t\t\t// Find out where we'll be inserting this block in the block index\n" +
            "\t\t\t\tBlockIndexEntry* idxEntry;\n" +
            "\t\t\t\tif (!insert_block_index_entry<allocMode>(idxEntry, currentTailIndex)) {\n" +
            "\t\t\t\t\treturn false;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Get ahold of a new block\n" +
            "\t\t\t\tauto newBlock = this->parent->ConcurrentQueue::template requisition_block<allocMode>();\n" +
            "\t\t\t\tif (newBlock == nullptr) {\n" +
            "\t\t\t\t\trewind_block_index_tail();\n" +
            "\t\t\t\t\tidxEntry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\treturn false;\n" +
            "\t\t\t\t}\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\t\t\tnewBlock->owner = this;\n" +
            "#endif\n" +
            "\t\t\t\tnewBlock->ConcurrentQueue::Block::template reset_empty<implicit_context>();\n" +
            "\n" +
            "\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (!MOODYCAMEL_NOEXCEPT_CTOR(T, U, new (static_cast<T*>(nullptr)) T(std::forward<U>(element)))) {\n" +
            "\t\t\t\t\t// May throw, try to insert now before we publish the fact that we have this new block\n" +
            "\t\t\t\t\tMOODYCAMEL_TRY {\n" +
            "\t\t\t\t\t\tnew ((*newBlock)[currentTailIndex]) T(std::forward<U>(element));\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tMOODYCAMEL_CATCH (...) {\n" +
            "\t\t\t\t\t\trewind_block_index_tail();\n" +
            "\t\t\t\t\t\tidxEntry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\tthis->parent->add_block_to_free_list(newBlock);\n" +
            "\t\t\t\t\t\tMOODYCAMEL_RETHROW;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t// Insert the new block into the index\n" +
            "\t\t\t\tidxEntry->value.store(newBlock, std::memory_order_relaxed);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tthis->tailBlock = newBlock;\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (!MOODYCAMEL_NOEXCEPT_CTOR(T, U, new (static_cast<T*>(nullptr)) T(std::forward<U>(element)))) {\n" +
            "\t\t\t\t\tthis->tailIndex.store(newTailIndex, std::memory_order_release);\n" +
            "\t\t\t\t\treturn true;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Enqueue\n" +
            "\t\t\tnew ((*this->tailBlock)[currentTailIndex]) T(std::forward<U>(element));\n" +
            "\t\t\t\n" +
            "\t\t\tthis->tailIndex.store(newTailIndex, std::memory_order_release);\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\ttemplate<typename U>\n" +
            "\t\tbool dequeue(U& element)\n" +
            "\t\t{\n" +
            "\t\t\t// See ExplicitProducer::dequeue for rationale and explanation\n" +
            "\t\t\tindex_t tail = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tindex_t overcommit = this->dequeueOvercommit.load(std::memory_order_relaxed);\n" +
            "\t\t\tif (details::circular_less_than<index_t>(this->dequeueOptimisticCount.load(std::memory_order_relaxed) - overcommit, tail)) {\n" +
            "\t\t\t\tstd::atomic_thread_fence(std::memory_order_acquire);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tindex_t myDequeueCount = this->dequeueOptimisticCount.fetch_add(1, std::memory_order_relaxed);\n" +
            "\t\t\t\ttail = this->tailIndex.load(std::memory_order_acquire);\n" +
            "\t\t\t\tif ((details::likely)(details::circular_less_than<index_t>(myDequeueCount - overcommit, tail))) {\n" +
            "\t\t\t\t\tindex_t index = this->headIndex.fetch_add(1, std::memory_order_acq_rel);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Determine which block the element is in\n" +
            "\t\t\t\t\tauto entry = get_block_index_entry_for_index(index);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Dequeue\n" +
            "\t\t\t\t\tauto block = entry->value.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\tauto& el = *((*block)[index]);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tif (!MOODYCAMEL_NOEXCEPT_ASSIGN(T, T&&, element = std::move(el))) {\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODBLOCKINDEX\n" +
            "\t\t\t\t\t\t// Note: Acquiring the mutex with every dequeue instead of only when a block\n" +
            "\t\t\t\t\t\t// is released is very sub-optimal, but it is, after all, purely debug code.\n" +
            "\t\t\t\t\t\tdebug::DebugLock lock(producer->mutex);\n" +
            "#endif\n" +
            "\t\t\t\t\t\tstruct Guard {\n" +
            "\t\t\t\t\t\t\tBlock* block;\n" +
            "\t\t\t\t\t\t\tindex_t index;\n" +
            "\t\t\t\t\t\t\tBlockIndexEntry* entry;\n" +
            "\t\t\t\t\t\t\tConcurrentQueue* parent;\n" +
            "\t\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\t\t~Guard()\n" +
            "\t\t\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\t\t(*block)[index]->~T();\n" +
            "\t\t\t\t\t\t\t\tif (block->ConcurrentQueue::Block::template set_empty<implicit_context>(index)) {\n" +
            "\t\t\t\t\t\t\t\t\tentry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t\t\t\tparent->add_block_to_free_list(block);\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t} guard = { block, index, entry, this->parent };\n" +
            "\n" +
            "\t\t\t\t\t\telement = std::move(el); // NOLINT\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\telse {\n" +
            "\t\t\t\t\t\telement = std::move(el); // NOLINT\n" +
            "\t\t\t\t\t\tel.~T(); // NOLINT\n" +
            "\n" +
            "\t\t\t\t\t\tif (block->ConcurrentQueue::Block::template set_empty<implicit_context>(index)) {\n" +
            "\t\t\t\t\t\t\t{\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODBLOCKINDEX\n" +
            "\t\t\t\t\t\t\t\tdebug::DebugLock lock(mutex);\n" +
            "#endif\n" +
            "\t\t\t\t\t\t\t\t// Add the block back into the global free pool (and remove from block index)\n" +
            "\t\t\t\t\t\t\t\tentry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\tthis->parent->add_block_to_free_list(block);\t\t// releases the above store\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\treturn true;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\tthis->dequeueOvercommit.fetch_add(1, std::memory_order_release);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\n" +
            "\t\t\treturn false;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "#ifdef _MSC_VER\n" +
            "#pragma warning(push)\n" +
            "#pragma warning(disable: 4706)  // assignment within conditional expression\n" +
            "#endif\n" +
            "\t\ttemplate<AllocationMode allocMode, typename It>\n" +
            "\t\tbool enqueue_bulk(It itemFirst, size_t count)\n" +
            "\t\t{\n" +
            "\t\t\t// First, we need to make sure we have enough room to enqueue all of the elements;\n" +
            "\t\t\t// this means pre-allocating blocks and putting them in the block index (but only if\n" +
            "\t\t\t// all the allocations succeeded).\n" +
            "\t\t\t\n" +
            "\t\t\t// Note that the tailBlock we start off with may not be owned by us any more;\n" +
            "\t\t\t// this happens if it was filled up exactly to the top (setting tailIndex to\n" +
            "\t\t\t// the first index of the next block which is not yet allocated), then dequeued\n" +
            "\t\t\t// completely (putting it on the free list) before we enqueue again.\n" +
            "\t\t\t\n" +
            "\t\t\tindex_t startTailIndex = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto startBlock = this->tailBlock;\n" +
            "\t\t\tBlock* firstAllocatedBlock = nullptr;\n" +
            "\t\t\tauto endBlock = this->tailBlock;\n" +
            "\t\t\t\n" +
            "\t\t\t// Figure out how many blocks we'll need to allocate, and do so\n" +
            "\t\t\tsize_t blockBaseDiff = ((startTailIndex + count - 1) & ~static_cast<index_t>(BLOCK_SIZE - 1)) - ((startTailIndex - 1) & ~static_cast<index_t>(BLOCK_SIZE - 1));\n" +
            "\t\t\tindex_t currentTailIndex = (startTailIndex - 1) & ~static_cast<index_t>(BLOCK_SIZE - 1);\n" +
            "\t\t\tif (blockBaseDiff > 0) {\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODBLOCKINDEX\n" +
            "\t\t\t\tdebug::DebugLock lock(mutex);\n" +
            "#endif\n" +
            "\t\t\t\tdo {\n" +
            "\t\t\t\t\tblockBaseDiff -= static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\tcurrentTailIndex += static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Find out where we'll be inserting this block in the block index\n" +
            "\t\t\t\t\tBlockIndexEntry* idxEntry = nullptr;  // initialization here unnecessary but compiler can't always tell\n" +
            "\t\t\t\t\tBlock* newBlock;\n" +
            "\t\t\t\t\tbool indexInserted = false;\n" +
            "\t\t\t\t\tauto head = this->headIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\tassert(!details::circular_less_than<index_t>(currentTailIndex, head));\n" +
            "\t\t\t\t\tbool full = !details::circular_less_than<index_t>(head, currentTailIndex + BLOCK_SIZE) || (MAX_SUBQUEUE_SIZE != details::const_numeric_max<size_t>::value && (MAX_SUBQUEUE_SIZE == 0 || MAX_SUBQUEUE_SIZE - BLOCK_SIZE < currentTailIndex - head));\n" +
            "\n" +
            "\t\t\t\t\tif (full || !(indexInserted = insert_block_index_entry<allocMode>(idxEntry, currentTailIndex)) || (newBlock = this->parent->ConcurrentQueue::template requisition_block<allocMode>()) == nullptr) {\n" +
            "\t\t\t\t\t\t// Index allocation or block allocation failed; revert any other allocations\n" +
            "\t\t\t\t\t\t// and index insertions done so far for this operation\n" +
            "\t\t\t\t\t\tif (indexInserted) {\n" +
            "\t\t\t\t\t\t\trewind_block_index_tail();\n" +
            "\t\t\t\t\t\t\tidxEntry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tcurrentTailIndex = (startTailIndex - 1) & ~static_cast<index_t>(BLOCK_SIZE - 1);\n" +
            "\t\t\t\t\t\tfor (auto block = firstAllocatedBlock; block != nullptr; block = block->next) {\n" +
            "\t\t\t\t\t\t\tcurrentTailIndex += static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\t\tidxEntry = get_block_index_entry_for_index(currentTailIndex);\n" +
            "\t\t\t\t\t\t\tidxEntry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t\trewind_block_index_tail();\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tthis->parent->add_blocks_to_free_list(firstAllocatedBlock);\n" +
            "\t\t\t\t\t\tthis->tailBlock = startBlock;\n" +
            "\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\treturn false;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\t\t\t\tnewBlock->owner = this;\n" +
            "#endif\n" +
            "\t\t\t\t\tnewBlock->ConcurrentQueue::Block::template reset_empty<implicit_context>();\n" +
            "\t\t\t\t\tnewBlock->next = nullptr;\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Insert the new block into the index\n" +
            "\t\t\t\t\tidxEntry->value.store(newBlock, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Store the chain of blocks so that we can undo if later allocations fail,\n" +
            "\t\t\t\t\t// and so that we can find the blocks when we do the actual enqueueing\n" +
            "\t\t\t\t\tif ((startTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) != 0 || firstAllocatedBlock != nullptr) {\n" +
            "\t\t\t\t\t\tassert(this->tailBlock != nullptr);\n" +
            "\t\t\t\t\t\tthis->tailBlock->next = newBlock;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tthis->tailBlock = newBlock;\n" +
            "\t\t\t\t\tendBlock = newBlock;\n" +
            "\t\t\t\t\tfirstAllocatedBlock = firstAllocatedBlock == nullptr ? newBlock : firstAllocatedBlock;\n" +
            "\t\t\t\t} while (blockBaseDiff > 0);\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Enqueue, one block at a time\n" +
            "\t\t\tindex_t newTailIndex = startTailIndex + static_cast<index_t>(count);\n" +
            "\t\t\tcurrentTailIndex = startTailIndex;\n" +
            "\t\t\tthis->tailBlock = startBlock;\n" +
            "\t\t\tassert((startTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) != 0 || firstAllocatedBlock != nullptr || count == 0);\n" +
            "\t\t\tif ((startTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) == 0 && firstAllocatedBlock != nullptr) {\n" +
            "\t\t\t\tthis->tailBlock = firstAllocatedBlock;\n" +
            "\t\t\t}\n" +
            "\t\t\twhile (true) {\n" +
            "\t\t\t\tindex_t stopIndex = (currentTailIndex & ~static_cast<index_t>(BLOCK_SIZE - 1)) + static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\tif (details::circular_less_than<index_t>(newTailIndex, stopIndex)) {\n" +
            "\t\t\t\t\tstopIndex = newTailIndex;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tMOODYCAMEL_CONSTEXPR_IF (MOODYCAMEL_NOEXCEPT_CTOR(T, decltype(*itemFirst), new (static_cast<T*>(nullptr)) T(details::deref_noexcept(itemFirst)))) {\n" +
            "\t\t\t\t\twhile (currentTailIndex != stopIndex) {\n" +
            "\t\t\t\t\t\tnew ((*this->tailBlock)[currentTailIndex++]) T(*itemFirst++);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\tMOODYCAMEL_TRY {\n" +
            "\t\t\t\t\t\twhile (currentTailIndex != stopIndex) {\n" +
            "\t\t\t\t\t\t\tnew ((*this->tailBlock)[currentTailIndex]) T(details::nomove_if<!MOODYCAMEL_NOEXCEPT_CTOR(T, decltype(*itemFirst), new (static_cast<T*>(nullptr)) T(details::deref_noexcept(itemFirst)))>::eval(*itemFirst));\n" +
            "\t\t\t\t\t\t\t++currentTailIndex;\n" +
            "\t\t\t\t\t\t\t++itemFirst;\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tMOODYCAMEL_CATCH (...) {\n" +
            "\t\t\t\t\t\tauto constructedStopIndex = currentTailIndex;\n" +
            "\t\t\t\t\t\tauto lastBlockEnqueued = this->tailBlock;\n" +
            "\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\tif (!details::is_trivially_destructible<T>::value) {\n" +
            "\t\t\t\t\t\t\tauto block = startBlock;\n" +
            "\t\t\t\t\t\t\tif ((startTailIndex & static_cast<index_t>(BLOCK_SIZE - 1)) == 0) {\n" +
            "\t\t\t\t\t\t\t\tblock = firstAllocatedBlock;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\tcurrentTailIndex = startTailIndex;\n" +
            "\t\t\t\t\t\t\twhile (true) {\n" +
            "\t\t\t\t\t\t\t\tstopIndex = (currentTailIndex & ~static_cast<index_t>(BLOCK_SIZE - 1)) + static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\t\t\tif (details::circular_less_than<index_t>(constructedStopIndex, stopIndex)) {\n" +
            "\t\t\t\t\t\t\t\t\tstopIndex = constructedStopIndex;\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\twhile (currentTailIndex != stopIndex) {\n" +
            "\t\t\t\t\t\t\t\t\t(*block)[currentTailIndex++]->~T();\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\tif (block == lastBlockEnqueued) {\n" +
            "\t\t\t\t\t\t\t\t\tbreak;\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\tblock = block->next;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\tcurrentTailIndex = (startTailIndex - 1) & ~static_cast<index_t>(BLOCK_SIZE - 1);\n" +
            "\t\t\t\t\t\tfor (auto block = firstAllocatedBlock; block != nullptr; block = block->next) {\n" +
            "\t\t\t\t\t\t\tcurrentTailIndex += static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\t\tauto idxEntry = get_block_index_entry_for_index(currentTailIndex);\n" +
            "\t\t\t\t\t\t\tidxEntry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t\trewind_block_index_tail();\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tthis->parent->add_blocks_to_free_list(firstAllocatedBlock);\n" +
            "\t\t\t\t\t\tthis->tailBlock = startBlock;\n" +
            "\t\t\t\t\t\tMOODYCAMEL_RETHROW;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tif (this->tailBlock == endBlock) {\n" +
            "\t\t\t\t\tassert(currentTailIndex == newTailIndex);\n" +
            "\t\t\t\t\tbreak;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tthis->tailBlock = this->tailBlock->next;\n" +
            "\t\t\t}\n" +
            "\t\t\tthis->tailIndex.store(newTailIndex, std::memory_order_release);\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "#ifdef _MSC_VER\n" +
            "#pragma warning(pop)\n" +
            "#endif\n" +
            "\t\t\n" +
            "\t\ttemplate<typename It>\n" +
            "\t\tsize_t dequeue_bulk(It& itemFirst, size_t max)\n" +
            "\t\t{\n" +
            "\t\t\tauto tail = this->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto overcommit = this->dequeueOvercommit.load(std::memory_order_relaxed);\n" +
            "\t\t\tauto desiredCount = static_cast<size_t>(tail - (this->dequeueOptimisticCount.load(std::memory_order_relaxed) - overcommit));\n" +
            "\t\t\tif (details::circular_less_than<size_t>(0, desiredCount)) {\n" +
            "\t\t\t\tdesiredCount = desiredCount < max ? desiredCount : max;\n" +
            "\t\t\t\tstd::atomic_thread_fence(std::memory_order_acquire);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tauto myDequeueCount = this->dequeueOptimisticCount.fetch_add(desiredCount, std::memory_order_relaxed);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\ttail = this->tailIndex.load(std::memory_order_acquire);\n" +
            "\t\t\t\tauto actualCount = static_cast<size_t>(tail - (myDequeueCount - overcommit));\n" +
            "\t\t\t\tif (details::circular_less_than<size_t>(0, actualCount)) {\n" +
            "\t\t\t\t\tactualCount = desiredCount < actualCount ? desiredCount : actualCount;\n" +
            "\t\t\t\t\tif (actualCount < desiredCount) {\n" +
            "\t\t\t\t\t\tthis->dequeueOvercommit.fetch_add(desiredCount - actualCount, std::memory_order_release);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Get the first index. Note that since there's guaranteed to be at least actualCount elements, this\n" +
            "\t\t\t\t\t// will never exceed tail.\n" +
            "\t\t\t\t\tauto firstIndex = this->headIndex.fetch_add(actualCount, std::memory_order_acq_rel);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\t// Iterate the blocks and dequeue\n" +
            "\t\t\t\t\tauto index = firstIndex;\n" +
            "\t\t\t\t\tBlockIndexHeader* localBlockIndex;\n" +
            "\t\t\t\t\tauto indexIndex = get_block_index_index_for_index(index, localBlockIndex);\n" +
            "\t\t\t\t\tdo {\n" +
            "\t\t\t\t\t\tauto blockStartIndex = index;\n" +
            "\t\t\t\t\t\tindex_t endIndex = (index & ~static_cast<index_t>(BLOCK_SIZE - 1)) + static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\tendIndex = details::circular_less_than<index_t>(firstIndex + static_cast<index_t>(actualCount), endIndex) ? firstIndex + static_cast<index_t>(actualCount) : endIndex;\n" +
            "\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\tauto entry = localBlockIndex->index[indexIndex];\n" +
            "\t\t\t\t\t\tauto block = entry->value.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\tif (MOODYCAMEL_NOEXCEPT_ASSIGN(T, T&&, details::deref_noexcept(itemFirst) = std::move((*(*block)[index])))) {\n" +
            "\t\t\t\t\t\t\twhile (index != endIndex) {\n" +
            "\t\t\t\t\t\t\t\tauto& el = *((*block)[index]);\n" +
            "\t\t\t\t\t\t\t\t*itemFirst++ = std::move(el);\n" +
            "\t\t\t\t\t\t\t\tel.~T();\n" +
            "\t\t\t\t\t\t\t\t++index;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\telse {\n" +
            "\t\t\t\t\t\t\tMOODYCAMEL_TRY {\n" +
            "\t\t\t\t\t\t\t\twhile (index != endIndex) {\n" +
            "\t\t\t\t\t\t\t\t\tauto& el = *((*block)[index]);\n" +
            "\t\t\t\t\t\t\t\t\t*itemFirst = std::move(el);\n" +
            "\t\t\t\t\t\t\t\t\t++itemFirst;\n" +
            "\t\t\t\t\t\t\t\t\tel.~T();\n" +
            "\t\t\t\t\t\t\t\t\t++index;\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\tMOODYCAMEL_CATCH (...) {\n" +
            "\t\t\t\t\t\t\t\tdo {\n" +
            "\t\t\t\t\t\t\t\t\tentry = localBlockIndex->index[indexIndex];\n" +
            "\t\t\t\t\t\t\t\t\tblock = entry->value.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t\t\t\twhile (index != endIndex) {\n" +
            "\t\t\t\t\t\t\t\t\t\t(*block)[index++]->~T();\n" +
            "\t\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\t\t\t\tif (block->ConcurrentQueue::Block::template set_many_empty<implicit_context>(blockStartIndex, static_cast<size_t>(endIndex - blockStartIndex))) {\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODBLOCKINDEX\n" +
            "\t\t\t\t\t\t\t\t\t\tdebug::DebugLock lock(mutex);\n" +
            "#endif\n" +
            "\t\t\t\t\t\t\t\t\t\tentry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t\t\t\t\tthis->parent->add_block_to_free_list(block);\n" +
            "\t\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\t\tindexIndex = (indexIndex + 1) & (localBlockIndex->capacity - 1);\n" +
            "\t\t\t\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\t\t\t\tblockStartIndex = index;\n" +
            "\t\t\t\t\t\t\t\t\tendIndex = (index & ~static_cast<index_t>(BLOCK_SIZE - 1)) + static_cast<index_t>(BLOCK_SIZE);\n" +
            "\t\t\t\t\t\t\t\t\tendIndex = details::circular_less_than<index_t>(firstIndex + static_cast<index_t>(actualCount), endIndex) ? firstIndex + static_cast<index_t>(actualCount) : endIndex;\n" +
            "\t\t\t\t\t\t\t\t} while (index != firstIndex + actualCount);\n" +
            "\t\t\t\t\t\t\t\t\n" +
            "\t\t\t\t\t\t\t\tMOODYCAMEL_RETHROW;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tif (block->ConcurrentQueue::Block::template set_many_empty<implicit_context>(blockStartIndex, static_cast<size_t>(endIndex - blockStartIndex))) {\n" +
            "\t\t\t\t\t\t\t{\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODBLOCKINDEX\n" +
            "\t\t\t\t\t\t\t\tdebug::DebugLock lock(mutex);\n" +
            "#endif\n" +
            "\t\t\t\t\t\t\t\t// Note that the set_many_empty above did a release, meaning that anybody who acquires the block\n" +
            "\t\t\t\t\t\t\t\t// we're about to free can use it safely since our writes (and reads!) will have happened-before then.\n" +
            "\t\t\t\t\t\t\t\tentry->value.store(nullptr, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\tthis->parent->add_block_to_free_list(block);\t\t// releases the above store\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tindexIndex = (indexIndex + 1) & (localBlockIndex->capacity - 1);\n" +
            "\t\t\t\t\t} while (index != firstIndex + actualCount);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\treturn actualCount;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\tthis->dequeueOvercommit.fetch_add(desiredCount, std::memory_order_release);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\treturn 0;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\t// The block size must be > 1, so any number with the low bit set is an invalid block base index\n" +
            "\t\tstatic const index_t INVALID_BLOCK_BASE = 1;\n" +
            "\t\t\n" +
            "\t\tstruct BlockIndexEntry\n" +
            "\t\t{\n" +
            "\t\t\tstd::atomic<index_t> key;\n" +
            "\t\t\tstd::atomic<Block*> value;\n" +
            "\t\t};\n" +
            "\t\t\n" +
            "\t\tstruct BlockIndexHeader\n" +
            "\t\t{\n" +
            "\t\t\tsize_t capacity;\n" +
            "\t\t\tstd::atomic<size_t> tail;\n" +
            "\t\t\tBlockIndexEntry* entries;\n" +
            "\t\t\tBlockIndexEntry** index;\n" +
            "\t\t\tBlockIndexHeader* prev;\n" +
            "\t\t};\n" +
            "\t\t\n" +
            "\t\ttemplate<AllocationMode allocMode>\n" +
            "\t\tinline bool insert_block_index_entry(BlockIndexEntry*& idxEntry, index_t blockStartIndex)\n" +
            "\t\t{\n" +
            "\t\t\tauto localBlockIndex = blockIndex.load(std::memory_order_relaxed);\t\t// We're the only writer thread, relaxed is OK\n" +
            "\t\t\tif (localBlockIndex == nullptr) {\n" +
            "\t\t\t\treturn false;  // this can happen if new_block_index failed in the constructor\n" +
            "\t\t\t}\n" +
            "\t\t\tsize_t newTail = (localBlockIndex->tail.load(std::memory_order_relaxed) + 1) & (localBlockIndex->capacity - 1);\n" +
            "\t\t\tidxEntry = localBlockIndex->index[newTail];\n" +
            "\t\t\tif (idxEntry->key.load(std::memory_order_relaxed) == INVALID_BLOCK_BASE ||\n" +
            "\t\t\t\tidxEntry->value.load(std::memory_order_relaxed) == nullptr) {\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tidxEntry->key.store(blockStartIndex, std::memory_order_relaxed);\n" +
            "\t\t\t\tlocalBlockIndex->tail.store(newTail, std::memory_order_release);\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// No room in the old block index, try to allocate another one!\n" +
            "\t\t\tMOODYCAMEL_CONSTEXPR_IF (allocMode == CannotAlloc) {\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t\t}\n" +
            "\t\t\telse if (!new_block_index()) {\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\tlocalBlockIndex = blockIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\tnewTail = (localBlockIndex->tail.load(std::memory_order_relaxed) + 1) & (localBlockIndex->capacity - 1);\n" +
            "\t\t\t\tidxEntry = localBlockIndex->index[newTail];\n" +
            "\t\t\t\tassert(idxEntry->key.load(std::memory_order_relaxed) == INVALID_BLOCK_BASE);\n" +
            "\t\t\t\tidxEntry->key.store(blockStartIndex, std::memory_order_relaxed);\n" +
            "\t\t\t\tlocalBlockIndex->tail.store(newTail, std::memory_order_release);\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline void rewind_block_index_tail()\n" +
            "\t\t{\n" +
            "\t\t\tauto localBlockIndex = blockIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tlocalBlockIndex->tail.store((localBlockIndex->tail.load(std::memory_order_relaxed) - 1) & (localBlockIndex->capacity - 1), std::memory_order_relaxed);\n";

            public static String concurrentqueue4 =
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline BlockIndexEntry* get_block_index_entry_for_index(index_t index) const\n" +
            "\t\t{\n" +
            "\t\t\tBlockIndexHeader* localBlockIndex;\n" +
            "\t\t\tauto idx = get_block_index_index_for_index(index, localBlockIndex);\n" +
            "\t\t\treturn localBlockIndex->index[idx];\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline size_t get_block_index_index_for_index(index_t index, BlockIndexHeader*& localBlockIndex) const\n" +
            "\t\t{\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODBLOCKINDEX\n" +
            "\t\t\tdebug::DebugLock lock(mutex);\n" +
            "#endif\n" +
            "\t\t\tindex &= ~static_cast<index_t>(BLOCK_SIZE - 1);\n" +
            "\t\t\tlocalBlockIndex = blockIndex.load(std::memory_order_acquire);\n" +
            "\t\t\tauto tail = localBlockIndex->tail.load(std::memory_order_acquire);\n" +
            "\t\t\tauto tailBase = localBlockIndex->index[tail]->key.load(std::memory_order_relaxed);\n" +
            "\t\t\tassert(tailBase != INVALID_BLOCK_BASE);\n" +
            "\t\t\t// Note: Must use division instead of shift because the index may wrap around, causing a negative\n" +
            "\t\t\t// offset, whose negativity we want to preserve\n" +
            "\t\t\tauto offset = static_cast<size_t>(static_cast<typename std::make_signed<index_t>::type>(index - tailBase) / BLOCK_SIZE);\n" +
            "\t\t\tsize_t idx = (tail + offset) & (localBlockIndex->capacity - 1);\n" +
            "\t\t\tassert(localBlockIndex->index[idx]->key.load(std::memory_order_relaxed) == index && localBlockIndex->index[idx]->value.load(std::memory_order_relaxed) != nullptr);\n" +
            "\t\t\treturn idx;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tbool new_block_index()\n" +
            "\t\t{\n" +
            "\t\t\tauto prev = blockIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\tsize_t prevCapacity = prev == nullptr ? 0 : prev->capacity;\n" +
            "\t\t\tauto entryCount = prev == nullptr ? nextBlockIndexCapacity : prevCapacity;\n" +
            "\t\t\tauto raw = static_cast<char*>((Traits::malloc)(\n" +
            "\t\t\t\tsizeof(BlockIndexHeader) +\n" +
            "\t\t\t\tstd::alignment_of<BlockIndexEntry>::value - 1 + sizeof(BlockIndexEntry) * entryCount +\n" +
            "\t\t\t\tstd::alignment_of<BlockIndexEntry*>::value - 1 + sizeof(BlockIndexEntry*) * nextBlockIndexCapacity));\n" +
            "\t\t\tif (raw == nullptr) {\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\tauto header = new (raw) BlockIndexHeader;\n" +
            "\t\t\tauto entries = reinterpret_cast<BlockIndexEntry*>(details::align_for<BlockIndexEntry>(raw + sizeof(BlockIndexHeader)));\n" +
            "\t\t\tauto index = reinterpret_cast<BlockIndexEntry**>(details::align_for<BlockIndexEntry*>(reinterpret_cast<char*>(entries) + sizeof(BlockIndexEntry) * entryCount));\n" +
            "\t\t\tif (prev != nullptr) {\n" +
            "\t\t\t\tauto prevTail = prev->tail.load(std::memory_order_relaxed);\n" +
            "\t\t\t\tauto prevPos = prevTail;\n" +
            "\t\t\t\tsize_t i = 0;\n" +
            "\t\t\t\tdo {\n" +
            "\t\t\t\t\tprevPos = (prevPos + 1) & (prev->capacity - 1);\n" +
            "\t\t\t\t\tindex[i++] = prev->index[prevPos];\n" +
            "\t\t\t\t} while (prevPos != prevTail);\n" +
            "\t\t\t\tassert(i == prevCapacity);\n" +
            "\t\t\t}\n" +
            "\t\t\tfor (size_t i = 0; i != entryCount; ++i) {\n" +
            "\t\t\t\tnew (entries + i) BlockIndexEntry;\n" +
            "\t\t\t\tentries[i].key.store(INVALID_BLOCK_BASE, std::memory_order_relaxed);\n" +
            "\t\t\t\tindex[prevCapacity + i] = entries + i;\n" +
            "\t\t\t}\n" +
            "\t\t\theader->prev = prev;\n" +
            "\t\t\theader->entries = entries;\n" +
            "\t\t\theader->index = index;\n" +
            "\t\t\theader->capacity = nextBlockIndexCapacity;\n" +
            "\t\t\theader->tail.store((prevCapacity - 1) & (nextBlockIndexCapacity - 1), std::memory_order_relaxed);\n" +
            "\t\t\t\n" +
            "\t\t\tblockIndex.store(header, std::memory_order_release);\n" +
            "\t\t\t\n" +
            "\t\t\tnextBlockIndexCapacity <<= 1;\n" +
            "\t\t\t\n" +
            "\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\tprivate:\n" +
            "\t\tsize_t nextBlockIndexCapacity;\n" +
            "\t\tstd::atomic<BlockIndexHeader*> blockIndex;\n" +
            "\n" +
            "#ifdef MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "\tpublic:\n" +
            "\t\tdetails::ThreadExitListener threadExitListener;\n" +
            "\tprivate:\n" +
            "#endif\n" +
            "\t\t\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "\tpublic:\n" +
            "\t\tImplicitProducer* nextImplicitProducer;\n" +
            "\tprivate:\n" +
            "#endif\n" +
            "\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODBLOCKINDEX\n" +
            "\t\tmutable debug::DebugMutex mutex;\n" +
            "#endif\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\tfriend struct MemStats;\n" +
            "#endif\n" +
            "\t};\n" +
            "\t\n" +
            "\t\n" +
            "\t//////////////////////////////////\n" +
            "\t// Block pool manipulation\n" +
            "\t//////////////////////////////////\n" +
            "\t\n" +
            "\tvoid populate_initial_block_list(size_t blockCount)\n" +
            "\t{\n" +
            "\t\tinitialBlockPoolSize = blockCount;\n" +
            "\t\tif (initialBlockPoolSize == 0) {\n" +
            "\t\t\tinitialBlockPool = nullptr;\n" +
            "\t\t\treturn;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinitialBlockPool = create_array<Block>(blockCount);\n" +
            "\t\tif (initialBlockPool == nullptr) {\n" +
            "\t\t\tinitialBlockPoolSize = 0;\n" +
            "\t\t}\n" +
            "\t\tfor (size_t i = 0; i < initialBlockPoolSize; ++i) {\n" +
            "\t\t\tinitialBlockPool[i].dynamicallyAllocated = false;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\tinline Block* try_get_block_from_initial_pool()\n" +
            "\t{\n" +
            "\t\tif (initialBlockPoolIndex.load(std::memory_order_relaxed) >= initialBlockPoolSize) {\n" +
            "\t\t\treturn nullptr;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tauto index = initialBlockPoolIndex.fetch_add(1, std::memory_order_relaxed);\n" +
            "\t\t\n" +
            "\t\treturn index < initialBlockPoolSize ? (initialBlockPool + index) : nullptr;\n" +
            "\t}\n" +
            "\t\n" +
            "\tinline void add_block_to_free_list(Block* block)\n" +
            "\t{\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\t\tblock->owner = nullptr;\n" +
            "#endif\n" +
            "\t\tfreeList.add(block);\n" +
            "\t}\n" +
            "\t\n" +
            "\tinline void add_blocks_to_free_list(Block* block)\n" +
            "\t{\n" +
            "\t\twhile (block != nullptr) {\n" +
            "\t\t\tauto next = block->next;\n" +
            "\t\t\tadd_block_to_free_list(block);\n" +
            "\t\t\tblock = next;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\tinline Block* try_get_block_from_free_list()\n" +
            "\t{\n" +
            "\t\treturn freeList.try_get();\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Gets a free block from one of the memory pools, or allocates a new one (if applicable)\n" +
            "\ttemplate<AllocationMode canAlloc>\n" +
            "\tBlock* requisition_block()\n" +
            "\t{\n" +
            "\t\tauto block = try_get_block_from_initial_pool();\n" +
            "\t\tif (block != nullptr) {\n" +
            "\t\t\treturn block;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tblock = try_get_block_from_free_list();\n" +
            "\t\tif (block != nullptr) {\n" +
            "\t\t\treturn block;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (canAlloc == CanAlloc) {\n" +
            "\t\t\treturn create<Block>();\n" +
            "\t\t}\n" +
            "\t\telse {\n" +
            "\t\t\treturn nullptr;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\n" +
            "#ifdef MCDBGQ_TRACKMEM\n" +
            "\tpublic:\n" +
            "\t\tstruct MemStats {\n" +
            "\t\t\tsize_t allocatedBlocks;\n" +
            "\t\t\tsize_t usedBlocks;\n" +
            "\t\t\tsize_t freeBlocks;\n" +
            "\t\t\tsize_t ownedBlocksExplicit;\n" +
            "\t\t\tsize_t ownedBlocksImplicit;\n" +
            "\t\t\tsize_t implicitProducers;\n" +
            "\t\t\tsize_t explicitProducers;\n" +
            "\t\t\tsize_t elementsEnqueued;\n" +
            "\t\t\tsize_t blockClassBytes;\n" +
            "\t\t\tsize_t queueClassBytes;\n" +
            "\t\t\tsize_t implicitBlockIndexBytes;\n" +
            "\t\t\tsize_t explicitBlockIndexBytes;\n" +
            "\t\t\t\n" +
            "\t\t\tfriend class ConcurrentQueue;\n" +
            "\t\t\t\n" +
            "\t\tprivate:\n" +
            "\t\t\tstatic MemStats getFor(ConcurrentQueue* q)\n" +
            "\t\t\t{\n" +
            "\t\t\t\tMemStats stats = { 0 };\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tstats.elementsEnqueued = q->size_approx();\n" +
            "\t\t\t\n" +
            "\t\t\t\tauto block = q->freeList.head_unsafe();\n" +
            "\t\t\t\twhile (block != nullptr) {\n" +
            "\t\t\t\t\t++stats.allocatedBlocks;\n" +
            "\t\t\t\t\t++stats.freeBlocks;\n" +
            "\t\t\t\t\tblock = block->freeListNext.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tfor (auto ptr = q->producerListTail.load(std::memory_order_acquire); ptr != nullptr; ptr = ptr->next_prod()) {\n" +
            "\t\t\t\t\tbool implicit = dynamic_cast<ImplicitProducer*>(ptr) != nullptr;\n" +
            "\t\t\t\t\tstats.implicitProducers += implicit ? 1 : 0;\n" +
            "\t\t\t\t\tstats.explicitProducers += implicit ? 0 : 1;\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tif (implicit) {\n" +
            "\t\t\t\t\t\tauto prod = static_cast<ImplicitProducer*>(ptr);\n" +
            "\t\t\t\t\t\tstats.queueClassBytes += sizeof(ImplicitProducer);\n" +
            "\t\t\t\t\t\tauto head = prod->headIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\tauto tail = prod->tailIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\tauto hash = prod->blockIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\tif (hash != nullptr) {\n" +
            "\t\t\t\t\t\t\tfor (size_t i = 0; i != hash->capacity; ++i) {\n" +
            "\t\t\t\t\t\t\t\tif (hash->index[i]->key.load(std::memory_order_relaxed) != ImplicitProducer::INVALID_BLOCK_BASE && hash->index[i]->value.load(std::memory_order_relaxed) != nullptr) {\n" +
            "\t\t\t\t\t\t\t\t\t++stats.allocatedBlocks;\n" +
            "\t\t\t\t\t\t\t\t\t++stats.ownedBlocksImplicit;\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\tstats.implicitBlockIndexBytes += hash->capacity * sizeof(typename ImplicitProducer::BlockIndexEntry);\n" +
            "\t\t\t\t\t\t\tfor (; hash != nullptr; hash = hash->prev) {\n" +
            "\t\t\t\t\t\t\t\tstats.implicitBlockIndexBytes += sizeof(typename ImplicitProducer::BlockIndexHeader) + hash->capacity * sizeof(typename ImplicitProducer::BlockIndexEntry*);\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tfor (; details::circular_less_than<index_t>(head, tail); head += BLOCK_SIZE) {\n" +
            "\t\t\t\t\t\t\t//auto block = prod->get_block_index_entry_for_index(head);\n" +
            "\t\t\t\t\t\t\t++stats.usedBlocks;\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\telse {\n" +
            "\t\t\t\t\t\tauto prod = static_cast<ExplicitProducer*>(ptr);\n" +
            "\t\t\t\t\t\tstats.queueClassBytes += sizeof(ExplicitProducer);\n" +
            "\t\t\t\t\t\tauto tailBlock = prod->tailBlock;\n" +
            "\t\t\t\t\t\tbool wasNonEmpty = false;\n" +
            "\t\t\t\t\t\tif (tailBlock != nullptr) {\n" +
            "\t\t\t\t\t\t\tauto block = tailBlock;\n" +
            "\t\t\t\t\t\t\tdo {\n" +
            "\t\t\t\t\t\t\t\t++stats.allocatedBlocks;\n" +
            "\t\t\t\t\t\t\t\tif (!block->ConcurrentQueue::Block::template is_empty<explicit_context>() || wasNonEmpty) {\n" +
            "\t\t\t\t\t\t\t\t\t++stats.usedBlocks;\n" +
            "\t\t\t\t\t\t\t\t\twasNonEmpty = wasNonEmpty || block != tailBlock;\n" +
            "\t\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t\t++stats.ownedBlocksExplicit;\n" +
            "\t\t\t\t\t\t\t\tblock = block->next;\n" +
            "\t\t\t\t\t\t\t} while (block != tailBlock);\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\tauto index = prod->blockIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\twhile (index != nullptr) {\n" +
            "\t\t\t\t\t\t\tstats.explicitBlockIndexBytes += sizeof(typename ExplicitProducer::BlockIndexHeader) + index->size * sizeof(typename ExplicitProducer::BlockIndexEntry);\n" +
            "\t\t\t\t\t\t\tindex = static_cast<typename ExplicitProducer::BlockIndexHeader*>(index->prev);\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tauto freeOnInitialPool = q->initialBlockPoolIndex.load(std::memory_order_relaxed) >= q->initialBlockPoolSize ? 0 : q->initialBlockPoolSize - q->initialBlockPoolIndex.load(std::memory_order_relaxed);\n" +
            "\t\t\t\tstats.allocatedBlocks += freeOnInitialPool;\n" +
            "\t\t\t\tstats.freeBlocks += freeOnInitialPool;\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tstats.blockClassBytes = sizeof(Block) * stats.allocatedBlocks;\n" +
            "\t\t\t\tstats.queueClassBytes += sizeof(ConcurrentQueue);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\treturn stats;\n" +
            "\t\t\t}\n" +
            "\t\t};\n" +
            "\t\t\n" +
            "\t\t// For debugging only. Not thread-safe.\n" +
            "\t\tMemStats getMemStats()\n" +
            "\t\t{\n" +
            "\t\t\treturn MemStats::getFor(this);\n" +
            "\t\t}\n" +
            "\tprivate:\n" +
            "\t\tfriend struct MemStats;\n" +
            "#endif\n" +
            "\t\n" +
            "\t\n" +
            "\t//////////////////////////////////\n" +
            "\t// Producer list manipulation\n" +
            "\t//////////////////////////////////\t\n" +
            "\t\n" +
            "\tProducerBase* recycle_or_create_producer(bool isExplicit)\n" +
            "\t{\n" +
            "\t\tbool recycled;\n" +
            "\t\treturn recycle_or_create_producer(isExplicit, recycled);\n" +
            "\t}\n" +
            "\t\n" +
            "\tProducerBase* recycle_or_create_producer(bool isExplicit, bool& recycled)\n" +
            "\t{\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODHASH\n" +
            "\t\tdebug::DebugLock lock(implicitProdMutex);\n" +
            "#endif\n" +
            "\t\t// Try to re-use one first\n" +
            "\t\tfor (auto ptr = producerListTail.load(std::memory_order_acquire); ptr != nullptr; ptr = ptr->next_prod()) {\n" +
            "\t\t\tif (ptr->inactive.load(std::memory_order_relaxed) && ptr->isExplicit == isExplicit) {\n" +
            "\t\t\t\tbool expected = true;\n" +
            "\t\t\t\tif (ptr->inactive.compare_exchange_strong(expected, /* desired */ false, std::memory_order_acquire, std::memory_order_relaxed)) {\n" +
            "\t\t\t\t\t// We caught one! It's been marked as activated, the caller can have it\n" +
            "\t\t\t\t\trecycled = true;\n" +
            "\t\t\t\t\treturn ptr;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\trecycled = false;\n" +
            "\t\treturn add_producer(isExplicit ? static_cast<ProducerBase*>(create<ExplicitProducer>(this)) : create<ImplicitProducer>(this));\n" +
            "\t}\n" +
            "\t\n" +
            "\tProducerBase* add_producer(ProducerBase* producer)\n" +
            "\t{\n" +
            "\t\t// Handle failed memory allocation\n" +
            "\t\tif (producer == nullptr) {\n" +
            "\t\t\treturn nullptr;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tproducerCount.fetch_add(1, std::memory_order_relaxed);\n" +
            "\t\t\n" +
            "\t\t// Add it to the lock-free list\n" +
            "\t\tauto prevTail = producerListTail.load(std::memory_order_relaxed);\n" +
            "\t\tdo {\n" +
            "\t\t\tproducer->next = prevTail;\n" +
            "\t\t} while (!producerListTail.compare_exchange_weak(prevTail, producer, std::memory_order_release, std::memory_order_relaxed));\n" +
            "\t\t\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "\t\tif (producer->isExplicit) {\n" +
            "\t\t\tauto prevTailExplicit = explicitProducers.load(std::memory_order_relaxed);\n" +
            "\t\t\tdo {\n" +
            "\t\t\t\tstatic_cast<ExplicitProducer*>(producer)->nextExplicitProducer = prevTailExplicit;\n" +
            "\t\t\t} while (!explicitProducers.compare_exchange_weak(prevTailExplicit, static_cast<ExplicitProducer*>(producer), std::memory_order_release, std::memory_order_relaxed));\n" +
            "\t\t}\n" +
            "\t\telse {\n" +
            "\t\t\tauto prevTailImplicit = implicitProducers.load(std::memory_order_relaxed);\n" +
            "\t\t\tdo {\n" +
            "\t\t\t\tstatic_cast<ImplicitProducer*>(producer)->nextImplicitProducer = prevTailImplicit;\n" +
            "\t\t\t} while (!implicitProducers.compare_exchange_weak(prevTailImplicit, static_cast<ImplicitProducer*>(producer), std::memory_order_release, std::memory_order_relaxed));\n" +
            "\t\t}\n" +
            "#endif\n" +
            "\t\t\n" +
            "\t\treturn producer;\n" +
            "\t}\n" +
            "\t\n" +
            "\tvoid reown_producers()\n" +
            "\t{\n" +
            "\t\t// After another instance is moved-into/swapped-with this one, all the\n" +
            "\t\t// producers we stole still think their parents are the other queue.\n" +
            "\t\t// So fix them up!\n" +
            "\t\tfor (auto ptr = producerListTail.load(std::memory_order_relaxed); ptr != nullptr; ptr = ptr->next_prod()) {\n" +
            "\t\t\tptr->parent = this;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\t\n" +
            "\t//////////////////////////////////\n" +
            "\t// Implicit producer hash\n" +
            "\t//////////////////////////////////\n" +
            "\t\n" +
            "\tstruct ImplicitProducerKVP\n" +
            "\t{\n" +
            "\t\tstd::atomic<details::thread_id_t> key;\n" +
            "\t\tImplicitProducer* value;\t\t// No need for atomicity since it's only read by the thread that sets it in the first place\n" +
            "\t\t\n" +
            "\t\tImplicitProducerKVP() : value(nullptr) { }\n" +
            "\t\t\n" +
            "\t\tImplicitProducerKVP(ImplicitProducerKVP&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t\t{\n" +
            "\t\t\tkey.store(other.key.load(std::memory_order_relaxed), std::memory_order_relaxed);\n" +
            "\t\t\tvalue = other.value;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline ImplicitProducerKVP& operator=(ImplicitProducerKVP&& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t\t{\n" +
            "\t\t\tswap(other);\n" +
            "\t\t\treturn *this;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\tinline void swap(ImplicitProducerKVP& other) MOODYCAMEL_NOEXCEPT\n" +
            "\t\t{\n" +
            "\t\t\tif (this != &other) {\n" +
            "\t\t\t\tdetails::swap_relaxed(key, other.key);\n" +
            "\t\t\t\tstd::swap(value, other.value);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\t\n" +
            "\ttemplate<typename XT, typename XTraits>\n" +
            "\tfriend void moodycamel::swap(typename ConcurrentQueue<XT, XTraits>::ImplicitProducerKVP&, typename ConcurrentQueue<XT, XTraits>::ImplicitProducerKVP&) MOODYCAMEL_NOEXCEPT;\n" +
            "\t\n" +
            "\tstruct ImplicitProducerHash\n" +
            "\t{\n" +
            "\t\tsize_t capacity;\n" +
            "\t\tImplicitProducerKVP* entries;\n" +
            "\t\tImplicitProducerHash* prev;\n" +
            "\t};\n" +
            "\t\n" +
            "\tinline void populate_initial_implicit_producer_hash()\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) {\n" +
            "\t\t\treturn;\n" +
            "\t\t}\n" +
            "\t\telse {\n" +
            "\t\t\timplicitProducerHashCount.store(0, std::memory_order_relaxed);\n" +
            "\t\t\tauto hash = &initialImplicitProducerHash;\n" +
            "\t\t\thash->capacity = INITIAL_IMPLICIT_PRODUCER_HASH_SIZE;\n" +
            "\t\t\thash->entries = &initialImplicitProducerHashEntries[0];\n" +
            "\t\t\tfor (size_t i = 0; i != INITIAL_IMPLICIT_PRODUCER_HASH_SIZE; ++i) {\n" +
            "\t\t\t\tinitialImplicitProducerHashEntries[i].key.store(details::invalid_thread_id, std::memory_order_relaxed);\n" +
            "\t\t\t}\n" +
            "\t\t\thash->prev = nullptr;\n" +
            "\t\t\timplicitProducerHash.store(hash, std::memory_order_relaxed);\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\tvoid swap_implicit_producer_hashes(ConcurrentQueue& other)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (INITIAL_IMPLICIT_PRODUCER_HASH_SIZE == 0) {\n" +
            "\t\t\treturn;\n" +
            "\t\t}\n" +
            "\t\telse {\n" +
            "\t\t\t// Swap (assumes our implicit producer hash is initialized)\n" +
            "\t\t\tinitialImplicitProducerHashEntries.swap(other.initialImplicitProducerHashEntries);\n" +
            "\t\t\tinitialImplicitProducerHash.entries = &initialImplicitProducerHashEntries[0];\n" +
            "\t\t\tother.initialImplicitProducerHash.entries = &other.initialImplicitProducerHashEntries[0];\n" +
            "\t\t\t\n" +
            "\t\t\tdetails::swap_relaxed(implicitProducerHashCount, other.implicitProducerHashCount);\n" +
            "\t\t\t\n" +
            "\t\t\tdetails::swap_relaxed(implicitProducerHash, other.implicitProducerHash);\n" +
            "\t\t\tif (implicitProducerHash.load(std::memory_order_relaxed) == &other.initialImplicitProducerHash) {\n" +
            "\t\t\t\timplicitProducerHash.store(&initialImplicitProducerHash, std::memory_order_relaxed);\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\tImplicitProducerHash* hash;\n" +
            "\t\t\t\tfor (hash = implicitProducerHash.load(std::memory_order_relaxed); hash->prev != &other.initialImplicitProducerHash; hash = hash->prev) {\n" +
            "\t\t\t\t\tcontinue;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\thash->prev = &initialImplicitProducerHash;\n" +
            "\t\t\t}\n" +
            "\t\t\tif (other.implicitProducerHash.load(std::memory_order_relaxed) == &initialImplicitProducerHash) {\n" +
            "\t\t\t\tother.implicitProducerHash.store(&other.initialImplicitProducerHash, std::memory_order_relaxed);\n" +
            "\t\t\t}\n" +
            "\t\t\telse {\n" +
            "\t\t\t\tImplicitProducerHash* hash;\n" +
            "\t\t\t\tfor (hash = other.implicitProducerHash.load(std::memory_order_relaxed); hash->prev != &initialImplicitProducerHash; hash = hash->prev) {\n" +
            "\t\t\t\t\tcontinue;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\thash->prev = &other.initialImplicitProducerHash;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\t// Only fails (returns nullptr) if memory allocation fails\n" +
            "\tImplicitProducer* get_or_add_implicit_producer()\n" +
            "\t{\n" +
            "\t\t// Note that since the data is essentially thread-local (key is thread ID),\n" +
            "\t\t// there's a reduced need for fences (memory ordering is already consistent\n" +
            "\t\t// for any individual thread), except for the current table itself.\n" +
            "\t\t\n" +
            "\t\t// Start by looking for the thread ID in the current and all previous hash tables.\n" +
            "\t\t// If it's not found, it must not be in there yet, since this same thread would\n" +
            "\t\t// have added it previously to one of the tables that we traversed.\n" +
            "\t\t\n" +
            "\t\t// Code and algorithm adapted from http://preshing.com/20130605/the-worlds-simplest-lock-free-hash-table\n" +
            "\t\t\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODHASH\n" +
            "\t\tdebug::DebugLock lock(implicitProdMutex);\n" +
            "#endif\n" +
            "\t\t\n" +
            "\t\tauto id = details::thread_id();\n" +
            "\t\tauto hashedId = details::hash_thread_id(id);\n" +
            "\t\t\n" +
            "\t\tauto mainHash = implicitProducerHash.load(std::memory_order_acquire);\n" +
            "\t\tassert(mainHash != nullptr);  // silence clang-tidy and MSVC warnings (hash cannot be null)\n" +
            "\t\tfor (auto hash = mainHash; hash != nullptr; hash = hash->prev) {\n" +
            "\t\t\t// Look for the id in this hash\n" +
            "\t\t\tauto index = hashedId;\n" +
            "\t\t\twhile (true) {\t\t// Not an infinite loop because at least one slot is free in the hash table\n" +
            "\t\t\t\tindex &= hash->capacity - 1;\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tauto probedKey = hash->entries[index].key.load(std::memory_order_relaxed);\n" +
            "\t\t\t\tif (probedKey == id) {\n" +
            "\t\t\t\t\t// Found it! If we had to search several hashes deep, though, we should lazily add it\n" +
            "\t\t\t\t\t// to the current main hash table to avoid the extended search next time.\n" +
            "\t\t\t\t\t// Note there's guaranteed to be room in the current hash table since every subsequent\n" +
            "\t\t\t\t\t// table implicitly reserves space for all previous tables (there's only one\n" +
            "\t\t\t\t\t// implicitProducerHashCount).\n" +
            "\t\t\t\t\tauto value = hash->entries[index].value;\n" +
            "\t\t\t\t\tif (hash != mainHash) {\n" +
            "\t\t\t\t\t\tindex = hashedId;\n" +
            "\t\t\t\t\t\twhile (true) {\n" +
            "\t\t\t\t\t\t\tindex &= mainHash->capacity - 1;\n" +
            "\t\t\t\t\t\t\tprobedKey = mainHash->entries[index].key.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\t\tauto empty = details::invalid_thread_id;\n" +
            "#ifdef MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "\t\t\t\t\t\t\tauto reusable = details::invalid_thread_id2;\n" +
            "\t\t\t\t\t\t\tif ((probedKey == empty    && mainHash->entries[index].key.compare_exchange_strong(empty,    id, std::memory_order_relaxed, std::memory_order_relaxed)) ||\n" +
            "\t\t\t\t\t\t\t\t(probedKey == reusable && mainHash->entries[index].key.compare_exchange_strong(reusable, id, std::memory_order_acquire, std::memory_order_acquire))) {\n" +
            "#else\n" +
            "\t\t\t\t\t\t\tif ((probedKey == empty    && mainHash->entries[index].key.compare_exchange_strong(empty,    id, std::memory_order_relaxed, std::memory_order_relaxed))) {\n" +
            "#endif\n" +
            "\t\t\t\t\t\t\t\tmainHash->entries[index].value = value;\n" +
            "\t\t\t\t\t\t\t\tbreak;\n" +
            "\t\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t\t\t++index;\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\treturn value;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tif (probedKey == details::invalid_thread_id) {\n" +
            "\t\t\t\t\tbreak;\t\t// Not in this hash table\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t++index;\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Insert!\n" +
            "\t\tauto newCount = 1 + implicitProducerHashCount.fetch_add(1, std::memory_order_relaxed);\n" +
            "\t\twhile (true) {\n" +
            "\t\t\t// NOLINTNEXTLINE(clang-analyzer-core.NullDereference)\n" +
            "\t\t\tif (newCount >= (mainHash->capacity >> 1) && !implicitProducerHashResizeInProgress.test_and_set(std::memory_order_acquire)) {\n" +
            "\t\t\t\t// We've acquired the resize lock, try to allocate a bigger hash table.\n" +
            "\t\t\t\t// Note the acquire fence synchronizes with the release fence at the end of this block, and hence when\n" +
            "\t\t\t\t// we reload implicitProducerHash it must be the most recent version (it only gets changed within this\n" +
            "\t\t\t\t// locked block).\n" +
            "\t\t\t\tmainHash = implicitProducerHash.load(std::memory_order_acquire);\n" +
            "\t\t\t\tif (newCount >= (mainHash->capacity >> 1)) {\n" +
            "\t\t\t\t\tauto newCapacity = mainHash->capacity << 1;\n" +
            "\t\t\t\t\twhile (newCount >= (newCapacity >> 1)) {\n" +
            "\t\t\t\t\t\tnewCapacity <<= 1;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tauto raw = static_cast<char*>((Traits::malloc)(sizeof(ImplicitProducerHash) + std::alignment_of<ImplicitProducerKVP>::value - 1 + sizeof(ImplicitProducerKVP) * newCapacity));\n" +
            "\t\t\t\t\tif (raw == nullptr) {\n" +
            "\t\t\t\t\t\t// Allocation failed\n" +
            "\t\t\t\t\t\timplicitProducerHashCount.fetch_sub(1, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\timplicitProducerHashResizeInProgress.clear(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\treturn nullptr;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tauto newHash = new (raw) ImplicitProducerHash;\n" +
            "\t\t\t\t\tnewHash->capacity = static_cast<size_t>(newCapacity);\n" +
            "\t\t\t\t\tnewHash->entries = reinterpret_cast<ImplicitProducerKVP*>(details::align_for<ImplicitProducerKVP>(raw + sizeof(ImplicitProducerHash)));\n" +
            "\t\t\t\t\tfor (size_t i = 0; i != newCapacity; ++i) {\n" +
            "\t\t\t\t\t\tnew (newHash->entries + i) ImplicitProducerKVP;\n" +
            "\t\t\t\t\t\tnewHash->entries[i].key.store(details::invalid_thread_id, std::memory_order_relaxed);\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\tnewHash->prev = mainHash;\n" +
            "\t\t\t\t\timplicitProducerHash.store(newHash, std::memory_order_release);\n" +
            "\t\t\t\t\timplicitProducerHashResizeInProgress.clear(std::memory_order_release);\n" +
            "\t\t\t\t\tmainHash = newHash;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\telse {\n" +
            "\t\t\t\t\timplicitProducerHashResizeInProgress.clear(std::memory_order_release);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// If it's < three-quarters full, add to the old one anyway so that we don't have to wait for the next table\n" +
            "\t\t\t// to finish being allocated by another thread (and if we just finished allocating above, the condition will\n" +
            "\t\t\t// always be true)\n" +
            "\t\t\tif (newCount < (mainHash->capacity >> 1) + (mainHash->capacity >> 2)) {\n" +
            "\t\t\t\tbool recycled;\n" +
            "\t\t\t\tauto producer = static_cast<ImplicitProducer*>(recycle_or_create_producer(false, recycled));\n" +
            "\t\t\t\tif (producer == nullptr) {\n" +
            "\t\t\t\t\timplicitProducerHashCount.fetch_sub(1, std::memory_order_relaxed);\n" +
            "\t\t\t\t\treturn nullptr;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\tif (recycled) {\n" +
            "\t\t\t\t\timplicitProducerHashCount.fetch_sub(1, std::memory_order_relaxed);\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t\n" +
            "#ifdef MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "\t\t\t\tproducer->threadExitListener.callback = &ConcurrentQueue::implicit_producer_thread_exited_callback;\n" +
            "\t\t\t\tproducer->threadExitListener.userData = producer;\n" +
            "\t\t\t\tdetails::ThreadExitNotifier::subscribe(&producer->threadExitListener);\n" +
            "#endif\n" +
            "\t\t\t\t\n" +
            "\t\t\t\tauto index = hashedId;\n" +
            "\t\t\t\twhile (true) {\n" +
            "\t\t\t\t\tindex &= mainHash->capacity - 1;\n" +
            "\t\t\t\t\tauto probedKey = mainHash->entries[index].key.load(std::memory_order_relaxed);\n" +
            "\t\t\t\t\t\n" +
            "\t\t\t\t\tauto empty = details::invalid_thread_id;\n" +
            "#ifdef MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "\t\t\t\t\tauto reusable = details::invalid_thread_id2;\n" +
            "\t\t\t\t\tif ((probedKey == empty    && mainHash->entries[index].key.compare_exchange_strong(empty,    id, std::memory_order_relaxed, std::memory_order_relaxed)) ||\n" +
            "\t\t\t\t\t\t(probedKey == reusable && mainHash->entries[index].key.compare_exchange_strong(reusable, id, std::memory_order_acquire, std::memory_order_acquire))) {\n" +
            "#else\n" +
            "\t\t\t\t\tif ((probedKey == empty    && mainHash->entries[index].key.compare_exchange_strong(empty,    id, std::memory_order_relaxed, std::memory_order_relaxed))) {\n" +
            "#endif\n" +
            "\t\t\t\t\t\tmainHash->entries[index].value = producer;\n" +
            "\t\t\t\t\t\tbreak;\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t\t++index;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\treturn producer;\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\t// Hmm, the old hash is quite full and somebody else is busy allocating a new one.\n" +
            "\t\t\t// We need to wait for the allocating thread to finish (if it succeeds, we add, if not,\n" +
            "\t\t\t// we try to allocate ourselves).\n" +
            "\t\t\tmainHash = implicitProducerHash.load(std::memory_order_acquire);\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "#ifdef MOODYCAMEL_CPP11_THREAD_LOCAL_SUPPORTED\n" +
            "\tvoid implicit_producer_thread_exited(ImplicitProducer* producer)\n" +
            "\t{\n" +
            "\t\t// Remove from thread exit listeners\n" +
            "\t\tdetails::ThreadExitNotifier::unsubscribe(&producer->threadExitListener);\n" +
            "\t\t\n" +
            "\t\t// Remove from hash\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODHASH\n" +
            "\t\tdebug::DebugLock lock(implicitProdMutex);\n" +
            "#endif\n" +
            "\t\tauto hash = implicitProducerHash.load(std::memory_order_acquire);\n" +
            "\t\tassert(hash != nullptr);\t\t// The thread exit listener is only registered if we were added to a hash in the first place\n" +
            "\t\tauto id = details::thread_id();\n" +
            "\t\tauto hashedId = details::hash_thread_id(id);\n" +
            "\t\tdetails::thread_id_t probedKey;\n" +
            "\t\t\n" +
            "\t\t// We need to traverse all the hashes just in case other threads aren't on the current one yet and are\n" +
            "\t\t// trying to add an entry thinking there's a free slot (because they reused a producer)\n" +
            "\t\tfor (; hash != nullptr; hash = hash->prev) {\n" +
            "\t\t\tauto index = hashedId;\n" +
            "\t\t\tdo {\n" +
            "\t\t\t\tindex &= hash->capacity - 1;\n" +
            "\t\t\t\tprobedKey = hash->entries[index].key.load(std::memory_order_relaxed);\n" +
            "\t\t\t\tif (probedKey == id) {\n" +
            "\t\t\t\t\thash->entries[index].key.store(details::invalid_thread_id2, std::memory_order_release);\n" +
            "\t\t\t\t\tbreak;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t\t++index;\n" +
            "\t\t\t} while (probedKey != details::invalid_thread_id);\t\t// Can happen if the hash has changed but we weren't put back in it yet, or if we weren't added to this hash in the first place\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "\t\t// Mark the queue as being recyclable\n" +
            "\t\tproducer->inactive.store(true, std::memory_order_release);\n" +
            "\t}\n" +
            "\t\n" +
            "\tstatic void implicit_producer_thread_exited_callback(void* userData)\n" +
            "\t{\n" +
            "\t\tauto producer = static_cast<ImplicitProducer*>(userData);\n" +
            "\t\tauto queue = producer->parent;\n" +
            "\t\tqueue->implicit_producer_thread_exited(producer);\n" +
            "\t}\n" +
            "#endif\n" +
            "\t\n" +
            "\t//////////////////////////////////\n" +
            "\t// Utility functions\n" +
            "\t//////////////////////////////////\n" +
            "\n" +
            "\ttemplate<typename TAlign>\n" +
            "\tstatic inline void* aligned_malloc(size_t size)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (std::alignment_of<TAlign>::value <= std::alignment_of<details::max_align_t>::value)\n" +
            "\t\t\treturn (Traits::malloc)(size);\n" +
            "\t\telse {\n" +
            "\t\t\tsize_t alignment = std::alignment_of<TAlign>::value;\n" +
            "\t\t\tvoid* raw = (Traits::malloc)(size + alignment - 1 + sizeof(void*));\n" +
            "\t\t\tif (!raw)\n" +
            "\t\t\t\treturn nullptr;\n" +
            "\t\t\tchar* ptr = details::align_for<TAlign>(reinterpret_cast<char*>(raw) + sizeof(void*));\n" +
            "\t\t\t*(reinterpret_cast<void**>(ptr) - 1) = raw;\n" +
            "\t\t\treturn ptr;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\n" +
            "\ttemplate<typename TAlign>\n" +
            "\tstatic inline void aligned_free(void* ptr)\n" +
            "\t{\n" +
            "\t\tMOODYCAMEL_CONSTEXPR_IF (std::alignment_of<TAlign>::value <= std::alignment_of<details::max_align_t>::value)\n" +
            "\t\t\treturn (Traits::free)(ptr);\n" +
            "\t\telse\n" +
            "\t\t\t(Traits::free)(ptr ? *(reinterpret_cast<void**>(ptr) - 1) : nullptr);\n" +
            "\t}\n" +
            "\n" +
            "\ttemplate<typename U>\n" +
            "\tstatic inline U* create_array(size_t count)\n" +
            "\t{\n" +
            "\t\tassert(count > 0);\n" +
            "\t\tU* p = static_cast<U*>(aligned_malloc<U>(sizeof(U) * count));\n" +
            "\t\tif (p == nullptr)\n" +
            "\t\t\treturn nullptr;\n" +
            "\n" +
            "\t\tfor (size_t i = 0; i != count; ++i)\n" +
            "\t\t\tnew (p + i) U();\n" +
            "\t\treturn p;\n" +
            "\t}\n" +
            "\n" +
            "\ttemplate<typename U>\n" +
            "\tstatic inline void destroy_array(U* p, size_t count)\n" +
            "\t{\n" +
            "\t\tif (p != nullptr) {\n" +
            "\t\t\tassert(count > 0);\n" +
            "\t\t\tfor (size_t i = count; i != 0; )\n" +
            "\t\t\t\t(p + --i)->~U();\n" +
            "\t\t}\n" +
            "\t\taligned_free<U>(p);\n" +
            "\t}\n" +
            "\n" +
            "\ttemplate<typename U>\n" +
            "\tstatic inline U* create()\n" +
            "\t{\n" +
            "\t\tvoid* p = aligned_malloc<U>(sizeof(U));\n" +
            "\t\treturn p != nullptr ? new (p) U : nullptr;\n" +
            "\t}\n" +
            "\n" +
            "\ttemplate<typename U, typename A1>\n" +
            "\tstatic inline U* create(A1&& a1)\n" +
            "\t{\n" +
            "\t\tvoid* p = aligned_malloc<U>(sizeof(U));\n" +
            "\t\treturn p != nullptr ? new (p) U(std::forward<A1>(a1)) : nullptr;\n" +
            "\t}\n" +
            "\n" +
            "\ttemplate<typename U>\n" +
            "\tstatic inline void destroy(U* p)\n" +
            "\t{\n" +
            "\t\tif (p != nullptr)\n" +
            "\t\t\tp->~U();\n" +
            "\t\taligned_free<U>(p);\n" +
            "\t}\n" +
            "\n" +
            "private:\n" +
            "\tstd::atomic<ProducerBase*> producerListTail;\n" +
            "\tstd::atomic<std::uint32_t> producerCount;\n" +
            "\t\n" +
            "\tstd::atomic<size_t> initialBlockPoolIndex;\n" +
            "\tBlock* initialBlockPool;\n" +
            "\tsize_t initialBlockPoolSize;\n" +
            "\t\n" +
            "#ifndef MCDBGQ_USEDEBUGFREELIST\n" +
            "\tFreeList<Block> freeList;\n" +
            "#else\n" +
            "\tdebug::DebugFreeList<Block> freeList;\n" +
            "#endif\n" +
            "\t\n" +
            "\tstd::atomic<ImplicitProducerHash*> implicitProducerHash;\n" +
            "\tstd::atomic<size_t> implicitProducerHashCount;\t\t// Number of slots logically used\n" +
            "\tImplicitProducerHash initialImplicitProducerHash;\n" +
            "\tstd::array<ImplicitProducerKVP, INITIAL_IMPLICIT_PRODUCER_HASH_SIZE> initialImplicitProducerHashEntries;\n" +
            "\tstd::atomic_flag implicitProducerHashResizeInProgress;\n" +
            "\t\n" +
            "\tstd::atomic<std::uint32_t> nextExplicitConsumerId;\n" +
            "\tstd::atomic<std::uint32_t> globalExplicitConsumerOffset;\n" +
            "\t\n" +
            "#ifdef MCDBGQ_NOLOCKFREE_IMPLICITPRODHASH\n" +
            "\tdebug::DebugMutex implicitProdMutex;\n" +
            "#endif\n" +
            "\t\n" +
            "#ifdef MOODYCAMEL_QUEUE_INTERNAL_DEBUG\n" +
            "\tstd::atomic<ExplicitProducer*> explicitProducers;\n" +
            "\tstd::atomic<ImplicitProducer*> implicitProducers;\n" +
            "#endif\n" +
            "};\n" +
            "\n" +
            "\n" +
            "template<typename T, typename Traits>\n" +
            "ProducerToken::ProducerToken(ConcurrentQueue<T, Traits>& queue)\n" +
            "\t: producer(queue.recycle_or_create_producer(true))\n" +
            "{\n" +
            "\tif (producer != nullptr) {\n" +
            "\t\tproducer->token = this;\n" +
            "\t}\n" +
            "}\n" +
            "\n" +
            "template<typename T, typename Traits>\n" +
            "ProducerToken::ProducerToken(BlockingConcurrentQueue<T, Traits>& queue)\n" +
            "\t: producer(reinterpret_cast<ConcurrentQueue<T, Traits>*>(&queue)->recycle_or_create_producer(true))\n" +
            "{\n" +
            "\tif (producer != nullptr) {\n" +
            "\t\tproducer->token = this;\n" +
            "\t}\n" +
            "}\n" +
            "\n" +
            "template<typename T, typename Traits>\n" +
            "ConsumerToken::ConsumerToken(ConcurrentQueue<T, Traits>& queue)\n" +
            "\t: itemsConsumedFromCurrent(0), currentProducer(nullptr), desiredProducer(nullptr)\n" +
            "{\n" +
            "\tinitialOffset = queue.nextExplicitConsumerId.fetch_add(1, std::memory_order_release);\n" +
            "\tlastKnownGlobalOffset = static_cast<std::uint32_t>(-1);\n" +
            "}\n" +
            "\n" +
            "template<typename T, typename Traits>\n" +
            "ConsumerToken::ConsumerToken(BlockingConcurrentQueue<T, Traits>& queue)\n" +
            "\t: itemsConsumedFromCurrent(0), currentProducer(nullptr), desiredProducer(nullptr)\n" +
            "{\n" +
            "\tinitialOffset = reinterpret_cast<ConcurrentQueue<T, Traits>*>(&queue)->nextExplicitConsumerId.fetch_add(1, std::memory_order_release);\n" +
            "\tlastKnownGlobalOffset = static_cast<std::uint32_t>(-1);\n" +
            "}\n" +
            "\n" +
            "template<typename T, typename Traits>\n" +
            "inline void swap(ConcurrentQueue<T, Traits>& a, ConcurrentQueue<T, Traits>& b) MOODYCAMEL_NOEXCEPT\n" +
            "{\n" +
            "\ta.swap(b);\n" +
            "}\n" +
            "\n" +
            "inline void swap(ProducerToken& a, ProducerToken& b) MOODYCAMEL_NOEXCEPT\n" +
            "{\n" +
            "\ta.swap(b);\n" +
            "}\n" +
            "\n" +
            "inline void swap(ConsumerToken& a, ConsumerToken& b) MOODYCAMEL_NOEXCEPT\n" +
            "{\n" +
            "\ta.swap(b);\n" +
            "}\n" +
            "\n" +
            "template<typename T, typename Traits>\n" +
            "inline void swap(typename ConcurrentQueue<T, Traits>::ImplicitProducerKVP& a, typename ConcurrentQueue<T, Traits>::ImplicitProducerKVP& b) MOODYCAMEL_NOEXCEPT\n" +
            "{\n" +
            "\ta.swap(b);\n" +
            "}\n" +
            "\n" +
            "}\n" +
            "\n" +
            "#if defined(_MSC_VER) && (!defined(_HAS_CXX17) || !_HAS_CXX17)\n" +
            "#pragma warning(pop)\n" +
            "#endif\n" +
            "\n" +
            "#if defined(__GNUC__) && !defined(__INTEL_COMPILER)\n" +
            "#pragma GCC diagnostic pop\n" +
            "#endif";

    public static String lightweightsemaphore = "// Provides an efficient implementation of a semaphore (LightweightSemaphore).\n" +
            "// This is an extension of Jeff Preshing's sempahore implementation (licensed \n" +
            "// under the terms of its separate zlib license) that has been adapted and\n" +
            "// extended by Cameron Desrochers.\n" +
            "\n" +
            "#pragma once\n" +
            "\n" +
            "#include <cstddef> // For std::size_t\n" +
            "#include <atomic>\n" +
            "#include <type_traits> // For std::make_signed<T>\n" +
            "\n" +
            "#if defined(_WIN32)\n" +
            "// Avoid including windows.h in a header; we only need a handful of\n" +
            "// items, so we'll redeclare them here (this is relatively safe since\n" +
            "// the API generally has to remain stable between Windows versions).\n" +
            "// I know this is an ugly hack but it still beats polluting the global\n" +
            "// namespace with thousands of generic names or adding a .cpp for nothing.\n" +
            "extern \"C\" {\n" +
            "\tstruct _SECURITY_ATTRIBUTES;\n" +
            "\t__declspec(dllimport) void* __stdcall CreateSemaphoreW(_SECURITY_ATTRIBUTES* lpSemaphoreAttributes, long lInitialCount, long lMaximumCount, const wchar_t* lpName);\n" +
            "\t__declspec(dllimport) int __stdcall CloseHandle(void* hObject);\n" +
            "\t__declspec(dllimport) unsigned long __stdcall WaitForSingleObject(void* hHandle, unsigned long dwMilliseconds);\n" +
            "\t__declspec(dllimport) int __stdcall ReleaseSemaphore(void* hSemaphore, long lReleaseCount, long* lpPreviousCount);\n" +
            "}\n" +
            "#elif defined(__MACH__)\n" +
            "#include <mach/mach.h>\n" +
            "#elif defined(__unix__)\n" +
            "#include <semaphore.h>\n" +
            "#endif\n" +
            "\n" +
            "namespace moodycamel\n" +
            "{\n" +
            "namespace details\n" +
            "{\n" +
            "\n" +
            "// Code in the mpmc_sema namespace below is an adaptation of Jeff Preshing's\n" +
            "// portable + lightweight semaphore implementations, originally from\n" +
            "// https://github.com/preshing/cpp11-on-multicore/blob/master/common/sema.h\n" +
            "// LICENSE:\n" +
            "// Copyright (c) 2015 Jeff Preshing\n" +
            "//\n" +
            "// This software is provided 'as-is', without any express or implied\n" +
            "// warranty. In no event will the authors be held liable for any damages\n" +
            "// arising from the use of this software.\n" +
            "//\n" +
            "// Permission is granted to anyone to use this software for any purpose,\n" +
            "// including commercial applications, and to alter it and redistribute it\n" +
            "// freely, subject to the following restrictions:\n" +
            "//\n" +
            "// 1. The origin of this software must not be misrepresented; you must not\n" +
            "//\tclaim that you wrote the original software. If you use this software\n" +
            "//\tin a product, an acknowledgement in the product documentation would be\n" +
            "//\tappreciated but is not required.\n" +
            "// 2. Altered source versions must be plainly marked as such, and must not be\n" +
            "//\tmisrepresented as being the original software.\n" +
            "// 3. This notice may not be removed or altered from any source distribution.\n" +
            "#if defined(_WIN32)\n" +
            "class Semaphore\n" +
            "{\n" +
            "private:\n" +
            "\tvoid* m_hSema;\n" +
            "\t\n" +
            "\tSemaphore(const Semaphore& other) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\tSemaphore& operator=(const Semaphore& other) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\n" +
            "public:\n" +
            "\tSemaphore(int initialCount = 0)\n" +
            "\t{\n" +
            "\t\tassert(initialCount >= 0);\n" +
            "\t\tconst long maxLong = 0x7fffffff;\n" +
            "\t\tm_hSema = CreateSemaphoreW(nullptr, initialCount, maxLong, nullptr);\n" +
            "\t\tassert(m_hSema);\n" +
            "\t}\n" +
            "\n" +
            "\t~Semaphore()\n" +
            "\t{\n" +
            "\t\tCloseHandle(m_hSema);\n" +
            "\t}\n" +
            "\n" +
            "\tbool wait()\n" +
            "\t{\n" +
            "\t\tconst unsigned long infinite = 0xffffffff;\n" +
            "\t\treturn WaitForSingleObject(m_hSema, infinite) == 0;\n" +
            "\t}\n" +
            "\t\n" +
            "\tbool try_wait()\n" +
            "\t{\n" +
            "\t\treturn WaitForSingleObject(m_hSema, 0) == 0;\n" +
            "\t}\n" +
            "\t\n" +
            "\tbool timed_wait(std::uint64_t usecs)\n" +
            "\t{\n" +
            "\t\treturn WaitForSingleObject(m_hSema, (unsigned long)(usecs / 1000)) == 0;\n" +
            "\t}\n" +
            "\n" +
            "\tvoid signal(int count = 1)\n" +
            "\t{\n" +
            "\t\twhile (!ReleaseSemaphore(m_hSema, count, nullptr));\n" +
            "\t}\n" +
            "};\n" +
            "#elif defined(__MACH__)\n" +
            "//---------------------------------------------------------\n" +
            "// Semaphore (Apple iOS and OSX)\n" +
            "// Can't use POSIX semaphores due to http://lists.apple.com/archives/darwin-kernel/2009/Apr/msg00010.html\n" +
            "//---------------------------------------------------------\n" +
            "class Semaphore\n" +
            "{\n" +
            "private:\n" +
            "\tsemaphore_t m_sema;\n" +
            "\n" +
            "\tSemaphore(const Semaphore& other) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\tSemaphore& operator=(const Semaphore& other) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\n" +
            "public:\n" +
            "\tSemaphore(int initialCount = 0)\n" +
            "\t{\n" +
            "\t\tassert(initialCount >= 0);\n" +
            "\t\tkern_return_t rc = semaphore_create(mach_task_self(), &m_sema, SYNC_POLICY_FIFO, initialCount);\n" +
            "\t\tassert(rc == KERN_SUCCESS);\n" +
            "\t\t(void)rc;\n" +
            "\t}\n" +
            "\n" +
            "\t~Semaphore()\n" +
            "\t{\n" +
            "\t\tsemaphore_destroy(mach_task_self(), m_sema);\n" +
            "\t}\n" +
            "\n" +
            "\tbool wait()\n" +
            "\t{\n" +
            "\t\treturn semaphore_wait(m_sema) == KERN_SUCCESS;\n" +
            "\t}\n" +
            "\t\n" +
            "\tbool try_wait()\n" +
            "\t{\n" +
            "\t\treturn timed_wait(0);\n" +
            "\t}\n" +
            "\t\n" +
            "\tbool timed_wait(std::uint64_t timeout_usecs)\n" +
            "\t{\n" +
            "\t\tmach_timespec_t ts;\n" +
            "\t\tts.tv_sec = static_cast<unsigned int>(timeout_usecs / 1000000);\n" +
            "\t\tts.tv_nsec = static_cast<int>((timeout_usecs % 1000000) * 1000);\n" +
            "\n" +
            "\t\t// added in OSX 10.10: https://developer.apple.com/library/prerelease/mac/documentation/General/Reference/APIDiffsMacOSX10_10SeedDiff/modules/Darwin.html\n" +
            "\t\tkern_return_t rc = semaphore_timedwait(m_sema, ts);\n" +
            "\t\treturn rc == KERN_SUCCESS;\n" +
            "\t}\n" +
            "\n" +
            "\tvoid signal()\n" +
            "\t{\n" +
            "\t\twhile (semaphore_signal(m_sema) != KERN_SUCCESS);\n" +
            "\t}\n" +
            "\n" +
            "\tvoid signal(int count)\n" +
            "\t{\n" +
            "\t\twhile (count-- > 0)\n" +
            "\t\t{\n" +
            "\t\t\twhile (semaphore_signal(m_sema) != KERN_SUCCESS);\n" +
            "\t\t}\n" +
            "\t}\n" +
            "};\n" +
            "#elif defined(__unix__)\n" +
            "//---------------------------------------------------------\n" +
            "// Semaphore (POSIX, Linux)\n" +
            "//---------------------------------------------------------\n" +
            "class Semaphore\n" +
            "{\n" +
            "private:\n" +
            "\tsem_t m_sema;\n" +
            "\n" +
            "\tSemaphore(const Semaphore& other) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\tSemaphore& operator=(const Semaphore& other) MOODYCAMEL_DELETE_FUNCTION;\n" +
            "\n" +
            "public:\n" +
            "\tSemaphore(int initialCount = 0)\n" +
            "\t{\n" +
            "\t\tassert(initialCount >= 0);\n" +
            "\t\tint rc = sem_init(&m_sema, 0, static_cast<unsigned int>(initialCount));\n" +
            "\t\tassert(rc == 0);\n" +
            "\t\t(void)rc;\n" +
            "\t}\n" +
            "\n" +
            "\t~Semaphore()\n" +
            "\t{\n" +
            "\t\tsem_destroy(&m_sema);\n" +
            "\t}\n" +
            "\n" +
            "\tbool wait()\n" +
            "\t{\n" +
            "\t\t// http://stackoverflow.com/questions/2013181/gdb-causes-sem-wait-to-fail-with-eintr-error\n" +
            "\t\tint rc;\n" +
            "\t\tdo {\n" +
            "\t\t\trc = sem_wait(&m_sema);\n" +
            "\t\t} while (rc == -1 && errno == EINTR);\n" +
            "\t\treturn rc == 0;\n" +
            "\t}\n" +
            "\n" +
            "\tbool try_wait()\n" +
            "\t{\n" +
            "\t\tint rc;\n" +
            "\t\tdo {\n" +
            "\t\t\trc = sem_trywait(&m_sema);\n" +
            "\t\t} while (rc == -1 && errno == EINTR);\n" +
            "\t\treturn rc == 0;\n" +
            "\t}\n" +
            "\n" +
            "\tbool timed_wait(std::uint64_t usecs)\n" +
            "\t{\n" +
            "\t\tstruct timespec ts;\n" +
            "\t\tconst int usecs_in_1_sec = 1000000;\n" +
            "\t\tconst int nsecs_in_1_sec = 1000000000;\n" +
            "\t\tclock_gettime(CLOCK_REALTIME, &ts);\n" +
            "\t\tts.tv_sec += (time_t)(usecs / usecs_in_1_sec);\n" +
            "\t\tts.tv_nsec += (long)(usecs % usecs_in_1_sec) * 1000;\n" +
            "\t\t// sem_timedwait bombs if you have more than 1e9 in tv_nsec\n" +
            "\t\t// so we have to clean things up before passing it in\n" +
            "\t\tif (ts.tv_nsec >= nsecs_in_1_sec) {\n" +
            "\t\t\tts.tv_nsec -= nsecs_in_1_sec;\n" +
            "\t\t\t++ts.tv_sec;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\tint rc;\n" +
            "\t\tdo {\n" +
            "\t\t\trc = sem_timedwait(&m_sema, &ts);\n" +
            "\t\t} while (rc == -1 && errno == EINTR);\n" +
            "\t\treturn rc == 0;\n" +
            "\t}\n" +
            "\n" +
            "\tvoid signal()\n" +
            "\t{\n" +
            "\t\twhile (sem_post(&m_sema) == -1);\n" +
            "\t}\n" +
            "\n" +
            "\tvoid signal(int count)\n" +
            "\t{\n" +
            "\t\twhile (count-- > 0)\n" +
            "\t\t{\n" +
            "\t\t\twhile (sem_post(&m_sema) == -1);\n" +
            "\t\t}\n" +
            "\t}\n" +
            "};\n" +
            "#else\n" +
            "#error Unsupported platform! (No semaphore wrapper available)\n" +
            "#endif\n" +
            "\n" +
            "}\t// end namespace details\n" +
            "\n" +
            "\n" +
            "//---------------------------------------------------------\n" +
            "// LightweightSemaphore\n" +
            "//---------------------------------------------------------\n" +
            "class LightweightSemaphore\n" +
            "{\n" +
            "public:\n" +
            "\ttypedef std::make_signed<std::size_t>::type ssize_t;\n" +
            "\n" +
            "private:\n" +
            "\tstd::atomic<ssize_t> m_count;\n" +
            "\tdetails::Semaphore m_sema;\n" +
            "\tint m_maxSpins;\n" +
            "\n" +
            "\tbool waitWithPartialSpinning(std::int64_t timeout_usecs = -1)\n" +
            "\t{\n" +
            "\t\tssize_t oldCount;\n" +
            "\t\tint spin = m_maxSpins;\n" +
            "\t\twhile (--spin >= 0)\n" +
            "\t\t{\n" +
            "\t\t\toldCount = m_count.load(std::memory_order_relaxed);\n" +
            "\t\t\tif ((oldCount > 0) && m_count.compare_exchange_strong(oldCount, oldCount - 1, std::memory_order_acquire, std::memory_order_relaxed))\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t\tstd::atomic_signal_fence(std::memory_order_acquire);\t // Prevent the compiler from collapsing the loop.\n" +
            "\t\t}\n" +
            "\t\toldCount = m_count.fetch_sub(1, std::memory_order_acquire);\n" +
            "\t\tif (oldCount > 0)\n" +
            "\t\t\treturn true;\n" +
            "\t\tif (timeout_usecs < 0)\n" +
            "\t\t{\n" +
            "\t\t\tif (m_sema.wait())\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\tif (timeout_usecs > 0 && m_sema.timed_wait((std::uint64_t)timeout_usecs))\n" +
            "\t\t\treturn true;\n" +
            "\t\t// At this point, we've timed out waiting for the semaphore, but the\n" +
            "\t\t// count is still decremented indicating we may still be waiting on\n" +
            "\t\t// it. So we have to re-adjust the count, but only if the semaphore\n" +
            "\t\t// wasn't signaled enough times for us too since then. If it was, we\n" +
            "\t\t// need to release the semaphore too.\n" +
            "\t\twhile (true)\n" +
            "\t\t{\n" +
            "\t\t\toldCount = m_count.load(std::memory_order_acquire);\n" +
            "\t\t\tif (oldCount >= 0 && m_sema.try_wait())\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t\tif (oldCount < 0 && m_count.compare_exchange_strong(oldCount, oldCount + 1, std::memory_order_relaxed, std::memory_order_relaxed))\n" +
            "\t\t\t\treturn false;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\n" +
            "\tssize_t waitManyWithPartialSpinning(ssize_t max, std::int64_t timeout_usecs = -1)\n" +
            "\t{\n" +
            "\t\tassert(max > 0);\n" +
            "\t\tssize_t oldCount;\n" +
            "\t\tint spin = m_maxSpins;\n" +
            "\t\twhile (--spin >= 0)\n" +
            "\t\t{\n" +
            "\t\t\toldCount = m_count.load(std::memory_order_relaxed);\n" +
            "\t\t\tif (oldCount > 0)\n" +
            "\t\t\t{\n" +
            "\t\t\t\tssize_t newCount = oldCount > max ? oldCount - max : 0;\n" +
            "\t\t\t\tif (m_count.compare_exchange_strong(oldCount, newCount, std::memory_order_acquire, std::memory_order_relaxed))\n" +
            "\t\t\t\t\treturn oldCount - newCount;\n" +
            "\t\t\t}\n" +
            "\t\t\tstd::atomic_signal_fence(std::memory_order_acquire);\n" +
            "\t\t}\n" +
            "\t\toldCount = m_count.fetch_sub(1, std::memory_order_acquire);\n" +
            "\t\tif (oldCount <= 0)\n" +
            "\t\t{\n" +
            "\t\t\tif ((timeout_usecs == 0) || (timeout_usecs < 0 && !m_sema.wait()) || (timeout_usecs > 0 && !m_sema.timed_wait((std::uint64_t)timeout_usecs)))\n" +
            "\t\t\t{\n" +
            "\t\t\t\twhile (true)\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\toldCount = m_count.load(std::memory_order_acquire);\n" +
            "\t\t\t\t\tif (oldCount >= 0 && m_sema.try_wait())\n" +
            "\t\t\t\t\t\tbreak;\n" +
            "\t\t\t\t\tif (oldCount < 0 && m_count.compare_exchange_strong(oldCount, oldCount + 1, std::memory_order_relaxed, std::memory_order_relaxed))\n" +
            "\t\t\t\t\t\treturn 0;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\tif (max > 1)\n" +
            "\t\t\treturn 1 + tryWaitMany(max - 1);\n" +
            "\t\treturn 1;\n" +
            "\t}\n" +
            "\n" +
            "public:\n" +
            "\tLightweightSemaphore(ssize_t initialCount = 0, int maxSpins = 10000) : m_count(initialCount), m_maxSpins(maxSpins)\n" +
            "\t{\n" +
            "\t\tassert(initialCount >= 0);\n" +
            "\t\tassert(maxSpins >= 0);\n" +
            "\t}\n" +
            "\n" +
            "\tbool tryWait()\n" +
            "\t{\n" +
            "\t\tssize_t oldCount = m_count.load(std::memory_order_relaxed);\n" +
            "\t\twhile (oldCount > 0)\n" +
            "\t\t{\n" +
            "\t\t\tif (m_count.compare_exchange_weak(oldCount, oldCount - 1, std::memory_order_acquire, std::memory_order_relaxed))\n" +
            "\t\t\t\treturn true;\n" +
            "\t\t}\n" +
            "\t\treturn false;\n" +
            "\t}\n" +
            "\n" +
            "\tbool wait()\n" +
            "\t{\n" +
            "\t\treturn tryWait() || waitWithPartialSpinning();\n" +
            "\t}\n" +
            "\n" +
            "\tbool wait(std::int64_t timeout_usecs)\n" +
            "\t{\n" +
            "\t\treturn tryWait() || waitWithPartialSpinning(timeout_usecs);\n" +
            "\t}\n" +
            "\n" +
            "\t// Acquires between 0 and (greedily) max, inclusive\n" +
            "\tssize_t tryWaitMany(ssize_t max)\n" +
            "\t{\n" +
            "\t\tassert(max >= 0);\n" +
            "\t\tssize_t oldCount = m_count.load(std::memory_order_relaxed);\n" +
            "\t\twhile (oldCount > 0)\n" +
            "\t\t{\n" +
            "\t\t\tssize_t newCount = oldCount > max ? oldCount - max : 0;\n" +
            "\t\t\tif (m_count.compare_exchange_weak(oldCount, newCount, std::memory_order_acquire, std::memory_order_relaxed))\n" +
            "\t\t\t\treturn oldCount - newCount;\n" +
            "\t\t}\n" +
            "\t\treturn 0;\n" +
            "\t}\n" +
            "\n" +
            "\t// Acquires at least one, and (greedily) at most max\n" +
            "\tssize_t waitMany(ssize_t max, std::int64_t timeout_usecs)\n" +
            "\t{\n" +
            "\t\tassert(max >= 0);\n" +
            "\t\tssize_t result = tryWaitMany(max);\n" +
            "\t\tif (result == 0 && max > 0)\n" +
            "\t\t\tresult = waitManyWithPartialSpinning(max, timeout_usecs);\n" +
            "\t\treturn result;\n" +
            "\t}\n" +
            "\t\n" +
            "\tssize_t waitMany(ssize_t max)\n" +
            "\t{\n" +
            "\t\tssize_t result = waitMany(max, -1);\n" +
            "\t\tassert(result > 0);\n" +
            "\t\treturn result;\n" +
            "\t}\n" +
            "\n" +
            "\tvoid signal(ssize_t count = 1)\n" +
            "\t{\n" +
            "\t\tassert(count >= 0);\n" +
            "\t\tssize_t oldCount = m_count.fetch_add(count, std::memory_order_release);\n" +
            "\t\tssize_t toRelease = -oldCount < count ? -oldCount : count;\n" +
            "\t\tif (toRelease > 0)\n" +
            "\t\t{\n" +
            "\t\t\tm_sema.signal((int)toRelease);\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\tstd::size_t availableApprox() const\n" +
            "\t{\n" +
            "\t\tssize_t count = m_count.load(std::memory_order_relaxed);\n" +
            "\t\treturn count > 0 ? static_cast<std::size_t>(count) : 0;\n" +
            "\t}\n" +
            "};\n" +
            "\n" +
            "}   // end namespace moodycamel";
}
