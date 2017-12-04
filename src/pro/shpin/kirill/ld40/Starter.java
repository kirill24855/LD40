package pro.shpin.kirill.ld40;

import pro.shpin.kirill.ld40.control.Engine;
import pro.shpin.kirill.ld40.model.Game;

public class Starter {

	public static void main(String[] args) {
		Game game = new Game();
		Engine engine;
		try {
			engine = new Engine("LD40", 1300, 700, game);
			engine.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
