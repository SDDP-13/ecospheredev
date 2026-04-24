package uk.ac.soton.comp2300.scene;

import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import uk.ac.soton.comp2300.model.game_logic.BuildingData;
import uk.ac.soton.comp2300.model.game_logic.Planet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

    private Node selectedNode;
    private Node outlineNode;

    private boolean buildMode = false;
    private Consumer<BuildingData> selectionListener;


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

        for (BuildingData building : model.getBuildingData()) {
            renderBuilding(building);
        }

    }

    public void setSelectionListener(Consumer<BuildingData> selectionListener) {
        this.selectionListener = selectionListener;
    }
    public Group getGroup() { return planetGroup; }
    public Sphere getSphere() { return sphere; }
    public Rotate getRotateY() { return rotateY; }
    public boolean isCursorValid() { return lastValidState; }
    public double getCursorTheta() { return lastTheta; }
    public double getCursorPhi() { return lastPhi; }
    public boolean isDragging() { return dragging; }
    public void clearSelectionExternally() { clearSelection(); }

    public void renderBuilding(BuildingData buildingData) {
        double theta = buildingData.getTheta();
        double phi = buildingData.getPhi();

        double r = planetRadius;
        double x = r * Math.sin(theta) * Math.cos(phi);
        double y = r * Math.cos(theta);
        double z = r * Math.sin(theta) * Math.sin(phi);

        Node node = BuildingView.createNode(buildingData.getType());

        node.setPickOnBounds(true);
        node.setUserData(buildingData);
        node.setOnMouseClicked(e -> {
            e.consume();
            if (buildMode) return;
            onBuildingClicked(buildingData, node);
        });

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


    public void refreshBuildings() {
        planetGroup.getChildren().removeAll(buildingNodes.values());
        buildingNodes.clear();

        for (BuildingData buildingData : model.getBuildingData()) {
            renderBuilding(buildingData);
        }
    }

    public void setBuildMode(boolean enabled) {
        this.buildMode = enabled;
        clearSelection();
    }

    private void onBuildingClicked(BuildingData buildingData, Node node) {
        if (buildMode) return;

        clearSelection();

        selectedNode = node;
        outlineNode = createOutline(node);

        planetGroup.getChildren().add(outlineNode);

        if (selectionListener != null) selectionListener.accept(buildingData);
    }

    private Node createOutline(Node node) {

        if (!(node instanceof Group group)) {
            return null;
        }
        Node cylinder = group.getChildren().get(0);
        if (!(cylinder instanceof Cylinder original)) {
            return null;
        }

        Cylinder glow = new Cylinder(
                original.getRadius(),
                original.getHeight()
        );

        glow.setScaleX(1.3);
        glow.setScaleY(1.3);
        glow.setScaleZ(1.3);

        glow.setCullFace(CullFace.NONE);

        PhongMaterial glowMat = new PhongMaterial();
        glowMat.setSpecularColor(Color.LIMEGREEN);
        glowMat.setDiffuseColor(Color.rgb(50, 255, 50, 0.25));
        glowMat.setSpecularPower(0);
        glow.setMaterial(glowMat);


        glow.setDepthTest(DepthTest.DISABLE);


        glow.setTranslateX(group.getTranslateX());
        glow.setTranslateY(group.getTranslateY());
        glow.setTranslateZ(group.getTranslateZ());

        for (Transform t : group.getTransforms()) {
            glow.getTransforms().add(t.clone());
        }

        return glow;
    }

    private void clearSelection() {
        if (selectedNode != null) {
            selectedNode.setScaleX(1);
            selectedNode.setScaleY(1);
            selectedNode.setScaleZ(1);
            selectedNode = null;
        }

        if (outlineNode != null) {
            planetGroup.getChildren().remove(outlineNode);
            outlineNode = null;
        }
    }


    private void initBuildCursor() {
        buildCursor = new Sphere(6);

        validMaterial = new PhongMaterial();
        validMaterial.setSpecularColor(Color.WHITE);
        validMaterial.setDiffuseColor(Color.rgb(50, 255, 50, 0.3));

        invalidMaterial = new PhongMaterial();
        invalidMaterial.setSpecularColor(Color.WHITE);
        invalidMaterial.setDiffuseColor(Color.rgb(255, 50, 50, 0.3));

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
