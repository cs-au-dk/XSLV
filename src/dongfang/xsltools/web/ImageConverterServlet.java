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

public class ImageConverterServlet extends HttpServlet {

  /**
	 * 
	 */
	private static final long serialVersionUID = 5931453251004521717L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String key = request.getParameter("key");
    String type = request.getParameter("type");
    
    InteractiveRequestResponseBean validator = null;
    
    HttpSession session = request.getSession();

    if (session != null) {
      if (validator == null)
        validator = (InteractiveRequestResponseBean)session.getAttribute("validator");
      
      response.setBufferSize(10 * 1024 * 1024); // Improve on this!!! How to make it auto-flush??

      if (type == null)
        type = "PNG";
      
      if ("PNG".equals(type))
        response.setContentType("image/png");

      else if ("GIF".equals(type))
        response.setContentType("image/gif");

      else if ("JPG".equals(type))
        response.setContentType("image/jpeg"); // is that right???

      else if ("PS".equals(type))
        response.setContentType("application/postscript");

      // TODO: Default response type. What is that; application/binary???
            
      OutputStream os = response.getOutputStream();

      if (validator == null) {
        os.write("Your sessionhas expired!".getBytes());
      } else
        validator.getGeneratedResourceAsImage(key, os, type);
      //os.close();
    } else {
      response.getOutputStream().write("Your sessionhas expired!".getBytes());
      System.err.println("Null session in ImageConverterServlet!");
    }
  }
}
