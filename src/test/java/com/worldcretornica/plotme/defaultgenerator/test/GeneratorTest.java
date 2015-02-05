package com.worldcretornica.plotme.defaultgenerator.test;

import org.junit.Assert;
import org.junit.Test;

import com.worldcretornica.plotme.defaultgenerator.DefaultPlotManager;

public class GeneratorTest {

	@Test
	public void testGetPlotID() {
		int road, size, x, z;

		for (road = 3; road < 10; road++) {
			for (size = 1; size < 30; size++) {
				for (x = -50; x < 50; x++) {
					for (z = -50; z < 50; z++) {
						Assert.assertEquals(oldPlotId(road, size, x, z), DefaultPlotManager.internalgetPlotId(road, size, x, z));
					}
				}
			}
		}
	}

	private String oldPlotId(int pathsize, int size, int posx, int posz) {
		int valx = posx;
		int valz = posz;

		boolean road = false;

		double n3;
		int mod2 = 0;
		int mod1 = 1;

		int x = (int) Math.ceil((double) valx / size);
		int z = (int) Math.ceil((double) valz / size);

		if (pathsize % 2 == 1) {
			n3 = Math.ceil(((double) pathsize) / 2);
			mod2 = -1;
		} else {
			n3 = Math.floor(((double) pathsize) / 2);
		}

		for (double i = n3; i >= 0; i--) {
			if ((valx - i + mod1) % size == 0 || (valx + i + mod2) % size == 0) {
				road = true;
				x = (int) Math.ceil((double) (valx - n3) / size);
			}
			if ((valz - i + mod1) % size == 0 || (valz + i + mod2) % size == 0) {
				road = true;
				z = (int) Math.ceil((double) (valz - n3) / size);
			}
		}

		if (road) {
			return "";
		} else
			return "" + x + ";" + z;
	}
}
