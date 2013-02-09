package dongfang.xsltools.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dongfang.xsltools.context.InteractiveRequestResponseBean;

public class DOTServlet extends HttpServlet {

  /**
	 * 
	 */
	private static final long serialVersionUID = 7991261811116959717L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String key = request.getParameter("key");

    InteractiveRequestResponseBean validator = null;
    
    HttpSession session = request.getSession();

    if (session != null) {
      if (validator == null)
        validator = (InteractiveRequestResponseBean)session.getAttribute("validator");
      
      response.setBufferSize(10 * 1024 * 1024); // Improve on this!!! How to make it auto-flush??
      response.setContentType("text/plain");
      
      Writer wr = response.getWriter();
      PrintWriter pwr = new PrintWriter(wr);
      
      validator.getGeneratedResourceAsDot(key, pwr);
      
      pwr.flush();
    } else
      System.err.println("Null session in ImageConverterServlet!");
  }
}
