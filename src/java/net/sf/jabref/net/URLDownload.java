/*  Copyright (C) 2003-2011 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.net;

import net.sf.jabref.imports.ImportFormatReader;

import java.awt.Component;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.CookieHandler;

import javax.swing.ProgressMonitorInputStream;

/**
 * @author Erik Putrycz erik.putrycz-at-nrc-cnrc.gc.ca
 */

public class URLDownload {  
    
    private URL source;
    private URLConnection con = null;
    private File dest;
    private Component parent;
    private String mimeType = null;
    private String content = null;
    private String encoding = null;

    private CookieHandler cm;

    /**
     * URL download to a string. After construction, call download() and then getStringContent().
     * @param _source The URL to download.
     */
    public URLDownload(URL _source) {
        this.source = _source;
        this.dest = null;
        this.parent = null;

        setCookieHandler();
    }

    /**
     * URL download to a file. After construction, call download().
     * @param _parent Parent component.
     * @param _source The URL to download.
     * @param _dest The file to download into.
     */
    public URLDownload(Component _parent, URL _source, File _dest) {
        source = _source;
        dest = _dest;
        parent = _parent;

        setCookieHandler();
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    private void setCookieHandler() {
        try {
            // This should set up JabRef to receive cookies properly
            if ((cm = CookieHandler.getDefault()) == null) {
                cm = new CookieHandlerImpl();
                CookieHandler.setDefault(cm);
            }
        } catch (SecurityException e) {
            // Setting or getting the system default cookie handler is forbidden
            // In this case cookie handling is not possible.
        }
    }

    public String getMimeType() {
        return mimeType;
    }

    public URLConnection getURLConnection() {
        return con;
    }

    /**
     * This method can be used after download() has been called, to get the contents
     * of the download, provided this URLDownload was created with the constructor
     * that takes no File argument.
     */
    public String getStringContent() {
        return content;
    }

    public void openConnectionOnly() throws IOException {
        con = source.openConnection();
        con.setRequestProperty("User-Agent", "Jabref");
        mimeType = con.getContentType();
    }

    public void download() throws IOException {

        if (con == null) {
            con = source.openConnection();
            con.setRequestProperty("User-Agent", "Jabref");
            mimeType = con.getContentType();
        }
        if (dest != null)
            downloadToFile();
        else
            downloadToString();
    }

    protected void downloadToString() throws IOException {

    	InputStream input = new BufferedInputStream(con.getInputStream());
        Writer output = new StringWriter();

        try
          {
            copy(input, output);
          }
        catch (IOException e)
          {
            e.printStackTrace();
          }
        finally
          {
            try
              {
                input.close();
                output.close();
              }
            catch (Exception ignored)
              {
              }
          }

        content = output.toString();
    }


    protected void downloadToFile() throws IOException {

    	InputStream input = new BufferedInputStream(con.getInputStream());
        OutputStream output =  new BufferedOutputStream(new FileOutputStream(dest));

        try
          {
            copy(input, output);
          }
        catch (IOException e)
          {
            e.printStackTrace();
          }
        finally
          {
            try
              {
                input.close();
                output.close();
              }
            catch (Exception ignored)
              {
              }
          }        
    }

    public void copy(InputStream in, OutputStream out) throws IOException
      {
        InputStream _in = new ProgressMonitorInputStream(parent, "Downloading " + source.toString(), in);
        byte[] buffer = new byte[512];
        while(true)
        {
            int bytesRead = _in.read(buffer);
            if(bytesRead == -1) break;
            out.write(buffer, 0, bytesRead);
        }        
      }

    public void copy(InputStream in, Writer out) throws IOException
      {
        InputStream _in = new ProgressMonitorInputStream(parent, "Downloading " + source.toString(), in);
        Reader r = encoding != null ? new InputStreamReader(_in, encoding) :
                ImportFormatReader.getReaderDefaultEncoding(_in);
        BufferedReader read = new BufferedReader(r);

        String line;
        while ((line = read.readLine()) != null) {
            out.write(line);
            out.write("\n");
        }
      }
}
