package dongfang.xsltools.web;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * This thing does transformation on the server side. It might be a better idea
 * to have to client do it? Than again maybe not, because that would reveal our
 * abuse of XSLT...
 */
public class XSLFServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1004948544868721775L;

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String filename = request.getScheme();
		System.err.println(filename);

		filename = request.getLocalAddr();
		System.err.println(filename);

		filename = request.getLocalName();
		System.err.println(filename);

		filename = request.getProtocol();
		System.err.println(filename);
		
		filename = 
			request.getContextPath();
		System.err.println(filename);

		filename = request.getLocalName();
		System.err.println(filename);

		filename = request.getPathTranslated();
		System.err.println(filename);

		filename = request.getPathInfo();
		System.err.println(filename);

		filename = request.getRequestURI();
		System.err.println(filename);

		filename = request.getScheme();
		System.err.println(filename);

		filename = request.getRequestURL().toString();
		System.err.println(filename);
		
		boolean something = false;
		for (Enumeration it = request.getHeaderNames(); it.hasMoreElements();) {
			Object o =  it.nextElement();
			Object oo = request.getHeader(o.toString());
			System.out.println(o + "-->" + oo);
			something = true;
		}
		if (!something)
			System.out.println("No headers");

		something = false;
		
		for (Enumeration it = request.getParameterNames(); it.hasMoreElements();) {
			Object o =  it.nextElement();
			Object oo = request.getParameter(o.toString());
			System.out.println(o + "-p->" + oo);
			something = true;
		}

		if (!something)
			System.out.println("No parameters");

		/*
		dongfang.xsltools.context.InteractiveRequestResponseBean validator = 
			(dongfang.xsltools.context.InteractiveRequestResponseBean) getServletContext()
				.getAttribute("validator");
		*/
		throw new RuntimeException();
	}
}
