package uk.ac.soton.comp2300.scene;

import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import uk.ac.soton.comp2300.model.game_logic.BuildingData;
import uk.ac.soton.comp2300.model.game_logic.Planet;

import java.util.HashMap;
import java.util.Map;

public class PlanetView {
    private Planet model;
    private Group planetGroup;
    private Sphere sphere;
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    private double anchorX, anchorY;
    private double anchorAngleX = 0, anchorAngleY = 0;

    private double planetRadius = 80;

    private Map<BuildingData, Node> buildingNodes = new HashMap<>();

    private Sphere buildCursor;
    private PhongMaterial validMaterial;
    private PhongMaterial invalidMaterial;
    private boolean lastValidState = false;
    private double lastTheta = Double.NaN;
    private double lastPhi = Double.NaN;

    private boolean dragging = false;
    private double dragThreshold = 5;
    private double pressX, pressY;



    public PlanetView(Planet model) {
        this.model = model;
        planetGroup = new Group();
        sphere = new Sphere(planetRadius);

        String fileName = model.getTextureID().toLowerCase() + ".png";
        Image planetTexture = new Image(getClass().getResourceAsStream("/images/planet_textures/" + fileName));

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(planetTexture);
        sphere.setMaterial(material);


        planetGroup.getChildren().addAll(sphere);
        planetGroup.getTransforms().addAll(rotateX, rotateY);
        initBuildCursor();

        /*
        addCubeOnSurface(0, 0, Color.RED);
        addCubeOnSurface(Math.PI, 0, Color.BLUE);
        addCubeOnSurface(Math.PI / 2, Math.PI, Color.GREEN);
        */

        for (BuildingData building : model.getBuildingData()) {
            renderBuilding(building);
        }

    }

    public Group getGroup() { return planetGroup; }
    public Sphere getSphere() { return sphere; }
    public boolean isCursorValid() { return lastValidState; }
    public double getCursorTheta() { return lastTheta; }
    public double getCursorPhi() { return lastPhi; }
    public boolean isDragging() { return dragging; }

    public void renderBuilding(BuildingData buildingData) {
        double theta = buildingData.getTheta();
        double phi = buildingData.getPhi();

        double r = planetRadius;
        double x = r * Math.sin(theta) * Math.cos(phi);
        double y = r * Math.cos(theta);
        double z = r * Math.sin(theta) * Math.sin(phi);

        Cylinder node = (Cylinder) BuildingView.createNode(buildingData.getType());

        Point3D dir = new Point3D(x, y, z).normalize();
        Point3D yAxis = new Point3D(0,1,0);
        Point3D axisOfRotation = yAxis.crossProduct(dir);
        double angle = Math.toDegrees(Math.acos(yAxis.dotProduct(dir)));

        if (!axisOfRotation.equals(Point3D.ZERO)) {
            node.getTransforms().add(new Rotate(angle, axisOfRotation));
        }

        node.setTranslateX(x);
        node.setTranslateY(y);
        node.setTranslateZ(z);

        planetGroup.getChildren().add(node);
        buildingNodes.put(buildingData, node);
    }

    public void removeBuilding(BuildingData buildingData) {
        Node node = buildingNodes.get(buildingData);

        if (node != null) {
            planetGroup.getChildren().remove(node);
            buildingNodes.remove(buildingData);
        }
    }


    private void initBuildCursor() {
        buildCursor = new Sphere(6);

        validMaterial = new PhongMaterial(Color.LIGHTGREEN);
        validMaterial.setSpecularColor(Color.WHITE);

        invalidMaterial = new PhongMaterial(Color.RED);
        invalidMaterial.setSpecularColor(Color.WHITE);

        buildCursor.setMaterial(validMaterial);
        buildCursor.setOpacity(0.5);
        buildCursor.setVisible(false);

        planetGroup.getChildren().addAll(buildCursor);
    }

    public void updateBuildCursor(double theta, double phi, boolean isValid) {

        if (Double.compare(theta, lastTheta) == 0 &&
            Double.compare(phi, lastPhi) == 0 &&
            isValid == lastValidState) return;

        double r = planetRadius + 5;
        double x = r * Math.sin(theta) * Math.cos(phi);
        double y = r * Math.cos(theta);
        double z = r * Math.sin(theta) * Math.sin(phi);

        buildCursor.setTranslateX(x);
        buildCursor.setTranslateY(y);
        buildCursor.setTranslateZ(z);

        buildCursor.setVisible(true);
        buildCursor.setMaterial(isValid ? validMaterial : invalidMaterial);

        lastTheta = theta;
        lastPhi = phi;
        lastValidState = isValid;
    }

    public void hideBuildCursor() { buildCursor.setVisible(false); }
    public void showBuildCursor() { buildCursor.setVisible(true); }

    public void addCubeOnSurface(double theta, double phi, Color color) {
        double r = planetRadius + 5;
        double x = r * Math.sin(theta) * Math.cos(phi);
        double y = r * Math.cos(theta);
        double z = r * Math.sin(theta) * Math.sin(phi);

        Box cube = new Box(10, 10,10);

        Group pivot = new Group();
        pivot.getChildren().add(cube);

        cube.setTranslateX(x);
        cube.setTranslateY(y);
        cube.setTranslateZ(z);

        PhongMaterial material = new PhongMaterial(color);
        cube.setMaterial(material);


        planetGroup.getChildren().add(pivot);
    }

    public void onMousePressed(MouseEvent e)  {
        anchorX = e.getSceneX();
        anchorY = e.getSceneY();
        anchorAngleX = rotateX.getAngle();
        anchorAngleY = rotateY.getAngle();

        dragging = false;
        pressX = e.getSceneX();
        pressY = e.getSceneY();
    }

    public void onMouseDragged(MouseEvent e) {
        rotateX.setAngle(anchorAngleX - (anchorY - e.getSceneY()) * 0.5);
        rotateY.setAngle(anchorAngleY + (anchorX - e.getSceneX()) * 0.5);

        if (!dragging) {
            double dx = e.getSceneX() - pressX;
            double dy = e.getSceneY() - pressY;
            if (Math.sqrt(dx * dx + dy * dy) > dragThreshold) dragging = true;
            }
    }

    public void onScroll(ScrollEvent e, PerspectiveCamera camera) {
        double newZ = camera.getTranslateZ() + e.getDeltaY();
        if (newZ < -400 && newZ > -1000) camera.setTranslateZ(newZ);
    }

}
