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
package org.constellation.admin.web.filter.gzip;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;

class GZipServletOutputStream extends ServletOutputStream {
  private OutputStream stream;

  public GZipServletOutputStream(OutputStream output)
      throws IOException {
    super();
    this.stream = output;
  }

  @Override
  public void close() throws IOException {
    this.stream.close();
  }

  @Override
  public void flush() throws IOException {
    this.stream.flush();
  }

  @Override
  public void write(byte b[]) throws IOException {
    this.stream.write(b);
  }

  @Override
  public void write(byte b[], int off, int len) throws IOException {
    this.stream.write(b, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    this.stream.write(b);
  }

  @Override
  public boolean isReady() {
    return false;
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {

  }
}
