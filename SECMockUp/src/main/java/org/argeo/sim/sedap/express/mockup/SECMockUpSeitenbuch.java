package org.argeo.sim.sedap.express.mockup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.swing.SwingUtilities;

import de.bundeswehr.sedap.express.mockup.SECMockUpHMI;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.SurfaceImageLayer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.terrain.LocalElevationModel;
import gov.nasa.worldwind.view.orbit.OrbitView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SECMockUpSeitenbuch extends SECMockUpHMI {
	private Sector modelSector;

	@Override
	protected Model initializeWorldWindModel() {
		Path gisDir = Paths.get("./libs");

		Model model = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);

		LocalElevationModel seitenbuchElevationModel;
		try {
			SurfaceImageLayer imageLayer = new SurfaceImageLayer();
			imageLayer.addImage(gisDir.resolve("seitenbuch-clipped-wgs84.jpg").toString());
			model.getLayers().add(imageLayer);

			// Create a local elevation model from the data.
			CompoundElevationModel compoundElevationModel = new CompoundElevationModel();

			seitenbuchElevationModel = new LocalElevationModel();
			seitenbuchElevationModel.addElevations(gisDir.resolve("seitenbuch-dem-wgs84-lowres.tif").toString());
			compoundElevationModel.addElevationModel(seitenbuchElevationModel);

			model.getGlobe().setElevationModel(compoundElevationModel);
			this.modelSector = seitenbuchElevationModel.getSector();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		return model;
	}

	@Override
	protected void initialize() {
		super.initialize();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				zoomTo(wwPanel, modelSector);
			}
		});

	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("SECMockUpSeitenbuch.fxml"));

			Parent root = loader.load();
			Scene scene = new Scene(root);

			primaryStage.setTitle("Protection Franconia (Seitenbuch MockUp)");
			primaryStage.setScene(scene);
			primaryStage.show();

			primaryStage.setOnCloseRequest(event -> {
				cleanUp();
				System.exit(0);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	public static void zoomTo(WorldWindow wwd, Sector sector) {
		Objects.requireNonNull(wwd);
		Objects.requireNonNull(sector);

		Box extent = Sector.computeBoundingBox(wwd.getModel().getGlobe(),
				wwd.getSceneController().getVerticalExaggeration(), sector);
		Angle fov = wwd.getView().getFieldOfView();
		double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();
		((OrbitView) wwd.getView()).setCenterPosition(new Position(sector.getCentroid(), 0d));
		((OrbitView) wwd.getView()).setZoom(zoom);
	}

}
