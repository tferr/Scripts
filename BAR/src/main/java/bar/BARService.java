
package bar;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import net.imagej.ImageJService;

@Plugin(type = Service.class)
public class BARService extends AbstractService implements ImageJService {

	@Parameter
	private ScriptService scriptService;

	@Override
	public void initialize() {
		// Register this namespace with the ScriptService so we can drop package prefixes
		// in script parameters, allowing:
		// @BAR
		// instead of
		// @bar.BAR
		scriptService.addAlias(bar.BAR.class);
	}
}
