import java.util.ArrayList;
import java.util.List;

public class Main {

    public static double tscalar = 0;

    public static void main(String[] args) {

        // create new sceneObjects ArrayList. initialise and store each object in it.
        List<SceneObjects> sceneObjects = new ArrayList<>();
        sceneObjects.add(new Sphere(6, 0, 0, 1));
        sceneObjects.add(new Sphere(12, 0, -5, 1.25));
        sceneObjects.add(new PointLight(6, 0.5, 5, 1, 10));

        Render render = new Render();

        // create camera object and initialise it
        Camera cam = new Camera(0, 0, 0, 1, 0, 0, 0, 1, 0, 70, 4, 3, 125);
        // each cam. method calculates the various properties of the camera
        cam.directionVector();
        cam.upVector();
        cam.rightVector();
        cam.imagePlane();


        // create a 2D array of rays with the size of resolution of the camera
        Ray[][] primaryRay = new Ray[(int) cam.getResX()][(int) cam.getResY()];
        // 2d array of rays for secondary bounces
        Ray[][] secondRay = new Ray[(int) cam.getResX()][(int) cam.getResY()];

        for (int j = 0; j < cam.getResY(); j++) {
            for (int i = 0; i < cam.getResX(); i++) {

                render.computePrimaryRays(cam, primaryRay, sceneObjects, i, j);
                //render.computeShadowRay(primaryRay, secondRay, sceneObjects, i, j);
                render.computeNextBounce(20000, primaryRay, secondRay, sceneObjects, i, j);

            }
        }


        render.drawScreen(cam, primaryRay, secondRay);
    }
}







