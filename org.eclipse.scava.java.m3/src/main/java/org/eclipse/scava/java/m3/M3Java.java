package org.eclipse.scava.java.m3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.FileLocator;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.library.lang.java.m3.internal.EclipseJavaCompiler;
import org.rascalmpl.uri.ILogicalSourceLocationResolver;
import org.rascalmpl.uri.ISourceLocationInput;
import org.rascalmpl.uri.ISourceLocationOutput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class M3Java {
	private IValueFactory vf = ValueFactoryFactory.getValueFactory();
	private Evaluator evaluator;

	public M3Java() {
		this.evaluator = createRascalEvaluator(vf);
	}

	/**
	 * Build an M3 model from a JAR.
	 *
	 * @param jar
	 *            /absolute/path/to/the/File.jar
	 * @return IValue the M3 model
	 */
	public IValue createM3FromJar(String jar) {
		EclipseJavaCompiler ejc = new EclipseJavaCompiler(vf);

		return ejc.createM3FromJarFile(vf.sourceLocation(jar), evaluator);
	}

	/**
	 * Build an M3 model from an Eclipse project.
	 * 
	 * Should only be invoked from an Eclipse context, ie. IWorkspaceRoot should
	 * be accessible.
	 *
	 * @param project
	 *            Simple name of the project in the workspace (ie. "MyProject")
	 * @return IValue the M3 model
	 */
	public IValue createM3FromEclipseProject(String project) throws URISyntaxException {
		org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseJavaCompiler ejc = new org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseJavaCompiler(
				vf);
		ISourceLocation projectLoc = vf.sourceLocation("project", project, "");

		IValue allM3s = ejc.createM3sFromEclipseProject(projectLoc, vf.bool(false), evaluator);
		IValue composedM3 = evaluator.call("composeJavaM3", projectLoc, allM3s);
		
		return composedM3;
	}

	/**
	 * Extract a multimap of the method invocations in the given {@code m3}
	 * model.
	 * 
	 * @param m3
	 *            An M3 model (extracted from a JAR or Eclipse project)
	 * @return a multimap of the method invocations relation
	 */
	public Multimap<String, String> extractMethodInvocations(IValue m3) {
		ISet mis = ((ISet) ((IConstructor) m3).asWithKeywordParameters().getParameter("methodInvocation"));

		return convertISetToMultimap(mis);
	}

	private Multimap<String, String> convertISetToMultimap(ISet set) {
		Multimap<String, String> map = ArrayListMultimap.create();

		set.forEach(e -> {
			ITuple t = (ITuple) e;
			ISourceLocation md = (ISourceLocation) t.get(0);
			ISourceLocation mi = (ISourceLocation) t.get(1);
			map.put(md.toString(), mi.toString());
		});

		return map;
	}

	private Evaluator createRascalEvaluator(IValueFactory vf) {
		GlobalEnvironment heap = new GlobalEnvironment();
		ModuleEnvironment module = new ModuleEnvironment("$m3java$", heap);
		PrintWriter stderr = new PrintWriter(System.err);
		PrintWriter stdout = new PrintWriter(System.out);
		Evaluator eval = new Evaluator(vf, stderr, stdout, module, heap);
		URIResolverRegistry registry = eval.getRascalResolver().getRegistry();
		ILogicalSourceLocationResolver resolver = new BundleURIResolver(registry);
		registry.registerLogical(resolver);

		eval.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());

		IRascalMonitor mon = new NullRascalMonitor();
		eval.doImport(mon, "lang::java::m3::Core");
		eval.doImport(mon, "lang::java::m3::AST");
		eval.doImport(mon, "lang::java::m3::TypeSymbol");

		return eval;
	}

	/**
	 * URI resolver for the bundleresource:/// scheme
	 */
	static class BundleURIResolver
			implements ISourceLocationOutput, ISourceLocationInput, ILogicalSourceLocationResolver {
		private URIResolverRegistry registry;
		private final static IValueFactory VF = ValueFactoryFactory.getValueFactory();

		public BundleURIResolver(URIResolverRegistry registry) {
			this.registry = registry;
		}

		public ISourceLocation getResourceURI(ISourceLocation loc) throws IOException {
			return registry.logicalToPhysical(loc);
		}

		@Override
		public String scheme() {
			return "bundleresource";
		}

		@Override
		public boolean supportsHost() {
			return false;
		}

		@Override
		public boolean exists(ISourceLocation loc) {
			return registry.exists(loc);
		}

		@Override
		public InputStream getInputStream(ISourceLocation loc) throws IOException {
			return registry.getInputStream(loc);
		}

		@Override
		public boolean isDirectory(ISourceLocation loc) {
			return registry.isDirectory(loc);
		}

		@Override
		public boolean isFile(ISourceLocation loc) {
			return registry.isFile(loc);
		}
		
		@Override
		public Charset getCharset(ISourceLocation loc) throws IOException {
			return registry.getCharset(loc);
		}

		@Override
		public long lastModified(ISourceLocation loc) throws IOException {
			return registry.lastModified(loc);
		}

		@Override
		public String[] list(ISourceLocation loc) throws IOException {
			return registry.listEntries(loc);
		}

		@Override
		public OutputStream getOutputStream(ISourceLocation loc, boolean append) throws IOException {
			return registry.getOutputStream(loc, append);
		}

		@Override
		public void mkDirectory(ISourceLocation loc) throws IOException {
			registry.mkDirectory(loc);
		}

		@Override
		public void remove(ISourceLocation loc) throws IOException {
			registry.remove(loc);
		}

		@Override
		public String authority() {
			return null;
		}

		@Override
		public ISourceLocation resolve(ISourceLocation uri) {
			try {
				ISourceLocation result;
				URL resolved = FileLocator.resolve(uri.getURI().toURL());
				try {
					if (resolved.getProtocol().equals("jar") && resolved.getPath().startsWith("file:/")) {
						result = VF.sourceLocation("jar", null, resolved.getPath().substring("file:".length()));
					} else {
						result = VF.sourceLocation(URIUtil.fixUnicode(resolved.toURI()));
					}
				} catch (URISyntaxException e) {
					// lets try to make a URI out of the URL.
					String path = resolved.getPath();
					if (path.startsWith("file:")) {
						path = path.substring(5);
					}
					result = VF.sourceLocation(URIUtil.create(resolved.getProtocol(), resolved.getAuthority(), path));
				}
				if (result == uri) {
					throw new IOException("could not resolve " + uri);
				}
				return result;
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
