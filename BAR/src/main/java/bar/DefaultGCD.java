package bar;

import org.scijava.ItemIO;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = BAR.GCD.class, name = BAR.GCD.NAME, attrs = { @Attr(name = "aliases", value = BAR.GCD.ALIASES) })
public class DefaultGCD implements BAR.GCD {

	// -- Inputs --

	// We want our GCD function to have two inputs. These are declared using @Parameter notation
	@Parameter
	private double a;

	@Parameter
	private double b;

	// -- Outputs --

	@Parameter(type = ItemIO.OUTPUT)
	private double result;

	@Override
	public void run() {
		result = computeGCD(a, b);
	}

	private double computeGCD(final double p1, final double p2) {
		return p2 == 0 ? p1 : computeGCD(p2, p1%p2);
	}
}
