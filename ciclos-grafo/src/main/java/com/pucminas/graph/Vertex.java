package com.pucminas.graph;

import java.awt.Point;

public class Vertex {
	final private String value;
	final private Point data;

	public Vertex(String id, Point data) {
		this.value = id;
		this.data = data;
	}

	public String getvalue() {
		return value;
	}

	@Override
	public String toString() {
		return "(" + data.getX() + "," + data.getY() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((data == null) ? 0 : data.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		Vertex other = (Vertex) obj;

		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}
}
