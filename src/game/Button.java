package game;

import game.OutSpread.modes;
import processing.core.PConstants;
import processing.core.PVector;
import processing.event.MouseEvent;

public final class Button {

	private final OutSpread p;
	private final int w, h, textSize;
	private final PVector position;
	private final String label;

	protected Button(OutSpread p, int x, int y, int w, int h, String label) {
		this.p = p;
		position = new PVector(x, y);
		this.w = w;
		this.h = h;
		this.label = label;
		p.registerMethod("mouseEvent", this);
		this.textSize = 36;
	}
	
	protected Button(OutSpread p, int x, int y, int w, int h, String label, int textSize) {
		this.p = p;
		position = new PVector(x, y);
		this.w = w;
		this.h = h;
		this.label = label;
		p.registerMethod("mouseEvent", this);
		this.textSize = textSize;
	}

	protected void draw() {
		p.rectMode(PConstants.CENTER); // todo move
		if (mouseOver()) {
			p.fill((p.hue(p.getGraphics().backgroundColor) + 180) % 360, 55, 100);
		} else {
			p.fill((p.hue(p.getGraphics().backgroundColor) + 180) % 360, 35, 100);
		}
		p.r.setSeed(10);
		p.r.rect(position.x, position.y, w, h);

		p.fill(0);
		p.textSize(textSize);
		p.textAlign(PConstants.CENTER, PConstants.CENTER);
		p.text(label, position.x, position.y);
	}

	protected void setPositionX(float x) {
		position.x = x;
	}
	
	protected void setPosition(float x, float y) {
		position.x = x;
		position.y = y;
	}

	private boolean mouseOver() {
		return (p.mouseX >= position.x - w / 2 && p.mouseX <= position.x + w / 2 && p.mouseY >= position.y - h / 2
				&& p.mouseY <= position.y + h / 2);
	}

	public void mouseEvent(MouseEvent e) {
		if (e.getAction() == processing.event.MouseEvent.CLICK && mouseOver()) {
			switch (label) {
				case "Play" :
					p.changeMode(modes.GAME);
					break;
				case "Settings" :
					p.changeMode(modes.SETTINGS);
					break;
				case "Help" :
					p.changeMode(modes.HELP);
					break;
				case "Back" :
					p.changeMode(modes.MENU);
					break;
				default :
					break;
			}
		}
	}

}
