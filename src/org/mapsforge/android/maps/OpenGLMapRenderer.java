/*
 * Copyright 2010 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.android.maps;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * <b>This implementation is unstable and for testing only.</b>
 */
class OpenGLMapRenderer implements android.opengl.GLSurfaceView.Renderer {
	private int arrayListIndex;
	private Bitmap bitmap;
	private int circleBufferHandle;
	private ByteBuffer circleByteBuffer;
	private CircleContainer circleContainer;
	private FloatBuffer circleVertexBuffer;
	private int color;
	private WayContainer complexWayContainer;
	private float[][] coordinates;
	private byte currentLayer;
	private byte currentLevel;
	private GL10 mGL;
	private GL11 mGL11;
	private ArrayList<ShapePaintContainer> objectsToDraw;
	private Paint paint;
	private ByteBuffer pixelBuffer;
	private ShapePaintContainer shapePaintContainer;
	private ArrayList<ArrayList<ShapePaintContainer>> shapePaintContainers;
	private int vboHandle;
	private ByteBuffer vbuffer;
	private FloatBuffer vertices;
	private ArrayList<ShapePaintContainer> wayList;

	/**
	 * Flag to indicate the status of the current frame.
	 */
	boolean frameReady;

	/**
	 * Creates a new OpenGlMapRenderer.
	 */
	OpenGLMapRenderer() {
		this.objectsToDraw = new ArrayList<ShapePaintContainer>(1024);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity(); // Reset The Current Modelview Matrix

		for (this.arrayListIndex = 0; this.arrayListIndex < this.objectsToDraw.size(); ++this.arrayListIndex) {
			this.shapePaintContainer = this.objectsToDraw.get(this.arrayListIndex);
			switch (this.shapePaintContainer.shapeContainer.getShapeType()) {
				case CIRCLE:
					this.circleContainer = (CircleContainer) this.shapePaintContainer.shapeContainer;

					this.paint = this.shapePaintContainer.paint;
					this.color = this.paint.getColor();

					this.circleVertexBuffer.clear();
					this.circleVertexBuffer.put(new float[] {
							(this.circleContainer.x / 128 - 1.0f),
							(this.circleContainer.y / 128 - 1.0f), 0f,
							(float) Color.red(this.color) / 256,
							(float) Color.green(this.color) / 256,
							(float) Color.blue(this.color) / 256, 1.0f });

					this.circleVertexBuffer.flip();

					// set point size
					this.mGL11.glPointSize(this.circleContainer.radius * 2);

					// bind the vertex buffer
					this.mGL11.glBindBuffer(GL11.GL_ARRAY_BUFFER, this.circleBufferHandle);
					// transfer data into video memory
					this.mGL11.glBufferData(GL11.GL_ARRAY_BUFFER, 4 * 7 * 1,
							this.circleVertexBuffer, GL11.GL_DYNAMIC_DRAW);

					this.mGL11.glEnableClientState(GL10.GL_VERTEX_ARRAY);
					this.mGL11.glEnableClientState(GL10.GL_COLOR_ARRAY);
					this.mGL11.glVertexPointer(3, GL10.GL_FLOAT, 7 * 4, 0);
					this.mGL11.glColorPointer(4, GL10.GL_FLOAT, 7 * 4, 3 * 4);

					this.mGL11.glDrawArrays(GL10.GL_POINTS, 0, 1);
					// unbind the buffer
					this.mGL11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

					break;

				case WAY:
					this.complexWayContainer = (WayContainer) this.shapePaintContainer.shapeContainer;
					this.coordinates = this.complexWayContainer.coordinates;
					this.paint = this.shapePaintContainer.paint;
					this.color = this.paint.getColor();

					boolean fillWay = (this.paint.getStyle() == Paint.Style.FILL);

					this.vertices.rewind();

					for (int j = 0; j < this.coordinates.length; ++j) {

						// prevent ArrayIndexOutOfBoundsException - ways without nodes don't
						// need to be drawn
						if (this.coordinates[j].length == 0) {
							continue;
						}

						this.vertices.clear();

						float[] nodes;

						// Logger.d("trying to draw way with " + this.coordinates[j].length
						// + " nodes.");

						if (fillWay) {
							// Logger.d(" triangulating..");
							// triangulate if we need to fill the polygon
							EarClippingTriangulation ec = new EarClippingTriangulation(
									this.coordinates[j]);
							nodes = ec.getTrianglesAsFloatArray();
						} else {
							// simply use the coordinates given
							nodes = this.coordinates[j];
						}

						int i;
						// Logger.d("adding " + nodes.length * 7 / 2 +
						// " nodes to vertices array."
						// + " vertices.remaining==" + vertices.remaining() + " limit=="
						// + vertices.limit());
						for (i = 0; i < nodes.length; i += 2) {
							// TODO: BufferOverflow exception on next line
							this.vertices.put(new float[] { (nodes[i] / 128 - 1.0f),
									(nodes[i + 1] / 128 - 1.0f), 0f,
									(float) Color.red(this.color) / 256,
									(float) Color.green(this.color) / 256,
									(float) Color.blue(this.color) / 256, 1.0f });
						}
						this.vertices.flip();

						// bind the buffer
						this.mGL11.glBindBuffer(GL11.GL_ARRAY_BUFFER, this.vboHandle);
						this.mGL11.glBufferData(GL11.GL_ARRAY_BUFFER, 4 * 7 * i / 2,
								this.vertices, GL11.GL_DYNAMIC_DRAW);

						this.mGL11.glEnableClientState(GL10.GL_VERTEX_ARRAY);
						this.mGL11.glEnableClientState(GL10.GL_COLOR_ARRAY);
						this.mGL11.glVertexPointer(3, GL10.GL_FLOAT, 7 * 4, 0);
						this.mGL11.glColorPointer(4, GL10.GL_FLOAT, 7 * 4, 3 * 4);

						this.mGL11.glLineWidth(this.paint.getStrokeWidth());
						// mGL11.glLineWidth(1.0f);
						this.mGL11.glPointSize(this.paint.getStrokeWidth());

						this.mGL11.glDrawArrays(GL10.GL_POINTS, 0, i / 2);

						if (fillWay) {
							this.mGL11.glDrawArrays(GL10.GL_LINE_LOOP, 0, i / 2);
							this.mGL11.glDrawArrays(GL10.GL_TRIANGLES, 0, i / 2);
						} else {
							this.mGL11.glDrawArrays(GL10.GL_LINE_STRIP, 0, i / 2);
						}

						// unbind the buffer
						this.mGL11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
					}
					break;
			}
		}
		this.objectsToDraw.clear();

		if (this.pixelBuffer != null) {

			this.mGL.glReadPixels(0, 0, this.bitmap.getWidth(), this.bitmap.getHeight(),
					GL10.GL_RGB, GL10.GL_UNSIGNED_SHORT_5_6_5, this.pixelBuffer);

			// // conversion from RGBA to RGB 565
			// int newLength = this.pixelBuffer.array().length / 2;
			// byte[] bufferArray = this.pixelBuffer.array();
			// byte tempByte;
			// for (int i = 0; i < newLength; i += 2) {
			// tempByte = (byte) ((0xF8 & bufferArray[i * 2 + 1]) << 2 | (0xFF & bufferArray[i *
			// 2 + 2]) >> 3);
			// bufferArray[i + 1] = (byte) (0xF8 & bufferArray[i * 2] | (0xFF & bufferArray[i *
			// 2 + 1]) >> 5);
			// bufferArray[i] = tempByte;
			// }

			if (!this.bitmap.isRecycled()) {
				this.bitmap.copyPixelsFromBuffer(this.pixelBuffer);
			}
			this.frameReady = true;
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Logger.d("onSurfaceChanged called, width: " + width + ", height: " + height);

		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, width, 0.0f, height, 0.0f, 1.0f);

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D); // needed for textured lines?
		// gl.glEnable(GL10.GL_BLEND); // needed for textured lines?
		// gl.glBlendFunc(GL10.GL_ONE, GL10.GL_SRC_COLOR); // needed for textured lines?
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL10.GL_DITHER);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glEnable(GL10.GL_POINT_SMOOTH);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// save gl10 and gl11 objects
		this.mGL = gl;
		this.mGL11 = (GL11) gl;

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glClearColor(0.9f, 0.9f, 0.9f, 1);

		// set up buffers for VBOs
		this.vbuffer = ByteBuffer.allocateDirect(4 * 7 * 10000);
		this.vbuffer.order(ByteOrder.nativeOrder());
		this.vertices = this.vbuffer.asFloatBuffer();

		this.circleByteBuffer = ByteBuffer.allocateDirect(4 * 7 * 100);
		this.circleByteBuffer.order(ByteOrder.nativeOrder());
		this.circleVertexBuffer = this.circleByteBuffer.asFloatBuffer();

		// handles
		int[] handle = new int[2];
		this.mGL11.glGenBuffers(2, handle, 0); // find unused buffers and save in handle[]
		this.vboHandle = handle[0];
		this.circleBufferHandle = handle[1];
	}

	/**
	 * @param drawWays
	 *            the ways to be rendered.
	 * @param layers
	 *            the number of layers.
	 * @param levelsPerLayer
	 *            the amount of levels per layer.
	 * @see DatabaseMapGenerator#drawWays(ArrayList, byte, byte)
	 */
	void drawWays(ArrayList<ArrayList<ArrayList<ShapePaintContainer>>> drawWays, byte layers,
			byte levelsPerLayer) {
		// extract all ways in all layers and all levels and add them to ArrayList
		for (this.currentLayer = 0; this.currentLayer < layers; ++this.currentLayer) {
			this.shapePaintContainers = drawWays.get(this.currentLayer);
			for (this.currentLevel = 0; this.currentLevel < levelsPerLayer; ++this.currentLevel) {
				this.wayList = this.shapePaintContainers.get(this.currentLevel);
				for (this.arrayListIndex = this.wayList.size() - 1; this.arrayListIndex >= 0; --this.arrayListIndex) {
					this.shapePaintContainer = this.wayList.get(this.arrayListIndex);
					this.objectsToDraw.add(this.shapePaintContainer);
				}
			}
		}
	}

	/**
	 * @param bitmap
	 *            the bitmap on which all future tiles need to be copied.
	 * @see DatabaseMapGenerator#setupMapGenerator(Bitmap)
	 */
	void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		byte[] bytes = new byte[2 * this.bitmap.getHeight() * this.bitmap.getWidth()];
		this.pixelBuffer = ByteBuffer.wrap(bytes);
	}
}