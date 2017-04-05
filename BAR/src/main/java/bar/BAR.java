package bar;

import net.imagej.ops.AbstractNamespace;
import net.imagej.ops.Namespace;
import net.imagej.ops.Op;
import net.imagej.ops.OpMethod;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;

@Plugin(type = Namespace.class)
public class BAR extends AbstractNamespace {

	@Parameter
	private ScriptService scriptService;

	@Override
	public String getName() {
		return "bar";
	}

	// -- BAR Namespace Op interfaces --

	// We can make all of our interfaces nested classes.
	// This allows references to take the form of "Namespace.Op" which
	// can make things easier to understand.

	public interface GCD extends Op {
		// Note that the name and aliases are prepended with Namespace.getName
		String NAME = "bar.gcd";
		String ALIASES = "bar.gcf";
	}

	// -- BAR Namespace built-in methods --

	// Built-in methods provide type-safe methods for accessing Ops
	// in a namespace.

	// We always provide an Object... constructor that can be passed directly to
	// the
	// OpService.run method

	@OpMethod(op = bar.BAR.GCD.class)
	public Object gcd(final Object... args) {
		return ops().run(bar.BAR.GCD.class, args);
	}

	// But we can also type-narrow our inputs and returns with our knowledge of
	// the Op
	// implementations

	@OpMethod(op = bar.BAR.GCD.class)
	public double gcd(final double a, final double b) {
		return (Double) ops().run(bar.BAR.GCD.class, a, b);
	}
}
