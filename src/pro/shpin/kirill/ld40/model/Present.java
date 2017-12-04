package pro.shpin.kirill.ld40.model;

public class Present {

	private static final float DROP_SPEED = 200f;

	public float x;
	public float y;

	public float timeThroughAnim;

	public boolean isPortable;

	public boolean isBomb;

	public boolean isBonus;

	public Present(float x, float y, boolean isBomb, boolean isBonus) {
		this.x = x;
		this.y = y;
		this.timeThroughAnim = 0f;
		this.isPortable = true;
		this.isBomb = isBomb;
		this.isBonus = isBonus;
	}

	public void fall(float time) {
		y -= DROP_SPEED*time;
	}
}
