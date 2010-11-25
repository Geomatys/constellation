/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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

    public BoundedCompletionService(final Executor executor, int permits) {
      this.executor = executor;
      this.semaphore = new Semaphore(permits);
      this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }

    @Override
    public Future<V> poll() {
      return this.completionQueue.poll();
    }

    @Override
    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
      return this.completionQueue.poll(timeout, unit);
    }

    @Override
    public Future<V> submit(Callable<V> task)  {
      if (task == null) throw new IllegalArgumentException();
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
    public Future<V> submit(Runnable task, V result) {
      if (task == null) throw new IllegalArgumentException();
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


