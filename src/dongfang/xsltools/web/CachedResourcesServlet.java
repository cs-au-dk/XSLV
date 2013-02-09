package dongfang.xsltools.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dongfang.xsltools.context.InteractiveRequestResponseBean;

public class CachedResourcesServlet extends HttpServlet {

  /**
	 * 
	 */
	private static final long serialVersionUID = -8730165834523263252L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    InteractiveRequestResponseBean validator = null;
    
    HttpSession session = request.getSession();

    if (session != null) {
      if (validator == null)
        validator = (InteractiveRequestResponseBean)session.getAttribute("validator");
      
      // TODO: Default response type. What is that; application/binary???

      response.setBufferSize(10 * 1024 * 1024); // Improve on this!!! How to make it auto-flush??
      response.setContentType("application/zip");
      
      OutputStream os = response.getOutputStream();
      validator.zipCachedResources(os);
    } else
      System.err.println("Null session in CachedResourcesServlet!");
  }
}
