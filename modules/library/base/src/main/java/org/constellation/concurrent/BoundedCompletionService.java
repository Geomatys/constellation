/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Taken from jacob Hookom's blog at http://weblogs.java.net/blog/jhook/archive/2008/12/accelerating_ap_1.html
 *
 * Exactly like ExecutorCompletionService, except uses a Semaphore to only permit X tasks to run concurrently
 * on the passed Executor.
 */
public class BoundedCompletionService<V> implements CompletionService<V> {

  private final Semaphore semaphore;
  private final Executor executor;
  private final BlockingQueue<Future<V>> completionQueue;

  // FutureTask to release Semaphore as completed
  private class BoundedFuture extends FutureTask {
        BoundedFuture(Callable<V> c) { super(c); }
        BoundedFuture(Runnable t, V r) { super(t, r); }
        @Override
        protected void done() {
        	semaphore.release();
        	completionQueue.add(this);
        }
    }

    public BoundedCompletionService(final Executor executor, final int permits) {
      this.executor = executor;
      this.semaphore = new Semaphore(permits);
      this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }

    @Override
    public Future<V> poll() {
      return this.completionQueue.poll();
    }

    @Override
    public Future<V> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
      return this.completionQueue.poll(timeout, unit);
    }

    @Override
    public Future<V> submit(final Callable<V> task)  {
      if (task == null) throw new IllegalArgumentException("task can not be null.");
      try {
        final BoundedFuture f = new BoundedFuture(task);
        this.semaphore.acquire(); // waits
        this.executor.execute(f);
	return f;
      } catch (InterruptedException e) {
        // do nothing
      }
      return null;
    }

    @Override
    public Future<V> submit(final Runnable task, final V result) {
      if (task == null) throw new IllegalArgumentException("Task can not be null.");
      try {
        final BoundedFuture f = new BoundedFuture(task, result);
        this.semaphore.acquire(); // waits
        this.executor.execute(f);
        return f;
      } catch (InterruptedException e) {
        // do nothing
      }
      return null;
    }

    @Override
    public Future<V> take() throws InterruptedException {
      return this.completionQueue.take();
    }
}


