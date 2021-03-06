package com.marketfinance.app.utils.threads

import android.util.Log
import java.lang.IllegalStateException

class ThreadManager {

    val TAG = "ThreadManager"

    private val threadPool = mutableListOf<Thread>()

    /**
     * Creates and starts a thread in the [threadPool]
     * @param threadName Name of thread when creating. Required for debugging/logging
     * @param thread The thread to add in the [threadPool]
     */
    fun createThread(threadName: String, thread: Thread) {
        Log.d(TAG, "[THREAD] Creating thread: $threadName")
        threadPool.add(thread)
        thread.start()
    }

    /**
     * Allows for starting of threads in the [threadPool]
     */
    fun startAllThreads() {
        Log.d(TAG, "[THREAD] Starting all threads")
        for (thread in threadPool) {
            try {
                thread.start()
            } catch (error: IllegalThreadStateException) {
                Log.e(TAG, "Unable to start thread. Thread is probably all ready running")
            }

        }
    }

    /**
     * Kills all threads in the [threadPool]. Does not handle any [InterruptedException]
     */
    fun killAllThreads() {
        Log.d(TAG, "[THREAD] Killing all threads")
        for (thread in threadPool) {
            thread.interrupt()
        }
    }


}