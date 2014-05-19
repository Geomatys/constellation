/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
