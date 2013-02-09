package dongfang.xsltools.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dongfang.xsltools.context.InteractiveRequestResponseBean;

public class XMLGraphServlet extends HttpServlet {

  /**
	 * 
	 */
	private static final long serialVersionUID = 8566315005727389026L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String key = request.getParameter("key");
    String s_includeOriginLabels = request.getParameter("includeOriginLabels");
    boolean includeOriginLabels = false;
    
    InteractiveRequestResponseBean validator = null;
    
    HttpSession session = request.getSession();

    if (session != null) {
      if (validator == null)
        validator = (InteractiveRequestResponseBean)session.getAttribute("validator");
      
      if (s_includeOriginLabels == null)
        includeOriginLabels = true;
      
      // TODO: Default response type. What is that; application/binary???

      response.setBufferSize(10 * 1024 * 1024); // Improve on this!!! How to make it auto-flush??
      response.setContentType("application/zip");
      
      OutputStream os = response.getOutputStream();

      /*
       * Add this to Serializer:
       *  
  / **
   * Stores XML graph in ZIP output stream. File structure is
   * as the <code>dir</code> directory in {@link #store(XMLGraph,String, boolean)}.
   * @param xg XML graph
   * @param zip the ZipOutputStream where files are stored. The caller must take care to close it.
   * @param store_origins if true, node origins are stored as strings (and ignored otherwise)
   * @throws IOException if files cannot be written 
   * /
  public void storeZip(XMLGraph xg, ZipOutputStream zip, boolean store_origins) throws IOException {
    Debug.println(1, "storing XML graph in " + zip);
    Document d = convert(xg, store_origins);
    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
    zip.putNextEntry(new ZipEntry("xg.xml"));
    xo.output(d, zip);
    zip.closeEntry();
    for (Map.Entry<String,Automaton> e : getAutomata().entrySet()) {
      zip.putNextEntry(new ZipEntry(e.getKey()));
      e.getValue().store(zip);
    }
  }
       */
      
      validator.getGeneratedResourceAsZip(key, os, includeOriginLabels);
    } else
      System.err.println("Null session in ImageConverterServlet!");
  }
}
