import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RenderMultiThreaded {

    public int progress = 0;
    List<Double> amplitudes = new ArrayList<>();


    public RenderMultiThreaded() {
    }

    public void brightnessDistribution(Camera cam, Ray[][] primaryRay) {
        for (int i = 0; i < cam.getResX(); i++) {
            for (int j = 0; j < cam.getResY(); j++) {
                if (primaryRay[i][j].getLightAmplitude() != 0) { // filter out zeros
                    amplitudes.add(primaryRay[i][j].getLightAmplitude());
                }
            }
        }
        Collections.sort(amplitudes);
    }

    double getQuantile(List<Double> data, double quantile) {
        int index = (int) Math.ceil(quantile * data.size()) - 1;
        return data.get(Math.max(index, 0));
    }

    // ... ,,, ~~~ ::: ;;; XXX *** 000 DDD ### @@@
    public void drawScreen(Camera cam, Ray[][] primaryRay) {
        amplitudes.add(0.0);
        double max = Collections.max(amplitudes) * cam.getISO();
        double q1 = (max * 0.08);
        double q2 = (max * 0.16);
        double q3 = (max * 0.24);
        double q4 = (max * 0.32);
        double q5 = (max * 0.40);
        double q6 = (max * 0.48);
        double q7 = (max * 0.56);
        double q8 = (max * 0.64);
        double q9 = (max * 0.72);
        double q10 = (max * 0.80);
        double q11 = (max * 0.88);
        double q12 = (max * 0.95);

        // iterate through each rays hit value and print the output
        System.out.print("|");
        for (int i = 0; i < cam.getResX(); i++) {
            System.out.print("-|-");
        }
        System.out.println("|");
        for (int j = 0; j < cam.getResY(); j++) {
            System.out.print("|");
            for (int i = 0; i < cam.getResX(); i++) {
                if (primaryRay[i][j].getLightAmplitude() >= q12) {
                    System.out.print("@@@");
                } else if (primaryRay[i][j].getLightAmplitude() >= q11) {
                    System.out.print("DDD");
                } else if (primaryRay[i][j].getLightAmplitude() >= q10) {
                    System.out.print("000");
                } else if (primaryRay[i][j].getLightAmplitude() >= q9) {
                    System.out.print("UUU");
                } else if (primaryRay[i][j].getLightAmplitude() >= q8) {
                    System.out.print("###");
                } else if (primaryRay[i][j].getLightAmplitude() >= q7) {
                    System.out.print("ZZZ");
                } else if (primaryRay[i][j].getLightAmplitude() >= q6) {
                    System.out.print("***");
                } else if (primaryRay[i][j].getLightAmplitude() >= q5) {
                    System.out.print("xxx");
                } else if (primaryRay[i][j].getLightAmplitude() >= q4) {
                    System.out.print("~~~");
                } else if (primaryRay[i][j].getLightAmplitude() >= q3) {
                    System.out.print(";;;");
                } else if (primaryRay[i][j].getLightAmplitude() >= q2) {
                    System.out.print(":::");
                } else if (primaryRay[i][j].getLightAmplitude() >= q1) {
                    System.out.print(",,,");
                } else if (primaryRay[i][j].getLightAmplitude() > 0) {
                    System.out.print("...");
                } else if (primaryRay[i][j].getLightAmplitude() == 0) {
                    System.out.print("   ");
                }
            }
            System.out.println("|");
        }
        System.out.print("|");
        for (int i = 0; i < cam.getResX(); i++) {
            System.out.print("---");
        }
        System.out.println("|");
    }

    public static void threadRenderSegmentation(int res, int nThreads, int[][] boundArray) {
        int bound = res / nThreads;
        int lower = 0, upper = 0;
        for (int i = 0; i < nThreads; i++) {
            if (i != nThreads - 1) {
                lower = i * bound;
                boundArray[i][0] = lower;
                upper = ((i + 1) * bound) - 1;
                boundArray[i][1] = upper;
            }
            if (i == nThreads - 1) {
                lower = i * bound;
                boundArray[i][0] = lower - 1;
                upper = res;
                boundArray[i][1] = upper - 1;
            }
        }
    }

    public void computePixels(List<SceneObjects> sceneObjectsList, Camera cam, int numRays, int numBounces) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        System.out.println("Threads available:" + numThreads);
        Ray[][] primaryRay = new Ray[cam.getResX()][cam.getResY()];
        Ray[][] nthRay = new Ray[cam.getResX()][cam.getResY()];
        int[][] boundArrayX = new int[numThreads][2]; // x bounds
        threadRenderSegmentation(cam.getResX(), numThreads, boundArrayX);
        int[][] boundArrayY = new int[numThreads][2]; // y bounds
        threadRenderSegmentation(cam.getResY(), numThreads, boundArrayY);

        System.out.print("|-");
        for (int l = 0; l < Runtime.getRuntime().availableProcessors() * 2; l++) {
            System.out.print("-");
        }
        System.out.println("-|");
        System.out.print("|-");

        for (int j = 0; j < cam.getResY(); j++) {
            for (int i = 0; i < cam.getResX(); i++) {
                computePrimaryRay(cam, primaryRay, sceneObjectsList, i, j);
            }
        }

        for (int segmenty = 0; segmenty < numThreads; segmenty++) {
            for (int segmentx = 0; segmentx < numThreads; segmentx++) {
                int finalSegmentx = segmentx;
                int finalSegmenty = segmenty;
                executor.execute(() -> marchIntersectionLogic(primaryRay, nthRay, sceneObjectsList, numRays, numBounces, boundArrayX[finalSegmentx][0], boundArrayX[finalSegmentx][1], boundArrayY[finalSegmenty][0], boundArrayY[finalSegmenty][1]));
            }
            System.out.print("--");
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                System.err.println("Executor did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted while waiting for termination.");
            Thread.currentThread().interrupt();  // Restore the interrupted status
        }


        System.out.println("-|");
        brightnessDistribution(cam, primaryRay);
        drawScreen(cam, primaryRay);
    }

    public void computePrimaryRay(Camera cam, Ray[][] primaryRay, List<SceneObjects> sceneObjectsList, int i, int j) {
        List<SceneObjects> visibleObjects = new ArrayList<>();
        primaryRay[i][j] = new Ray(cam.getPosX(), cam.getPosY(), cam.getPosZ());
        // update the rays index to the current pixel
        primaryRay[i][j].setPixelX(i);
        primaryRay[i][j].setPixelY(j);

        // calculate pixel position on the plane
        primaryRay[i][j].setPixelIndexX((((i + 0.5) / cam.getResX()) * 2) - 1);
        primaryRay[i][j].setPixelIndexY(1 - (((j + 0.5) / cam.getResY()) * 2));

        // calculate pixel position in the scene
        primaryRay[i][j].setPixelPosX(primaryRay[i][j].getPixelIndexX() * cam.getCamWidth() / 2);
        primaryRay[i][j].setPixelPosY(primaryRay[i][j].getPixelIndexY() * cam.getCamHeight() / 2);

        // set the primary ray direction
        // D = normCamD + rightvector * ScenePosX + upvector * ScenePosY
        primaryRay[i][j].setDirX(cam.getNormDirX() + cam.getNormRightX() * primaryRay[i][j].getPixelPosX() + cam.getNormUpX() * primaryRay[i][j].getPixelPosY());
        primaryRay[i][j].setDirY(cam.getNormDirY() + cam.getNormRightY() * primaryRay[i][j].getPixelPosX() + cam.getNormUpY() * primaryRay[i][j].getPixelPosY());
        primaryRay[i][j].setDirZ(cam.getNormDirZ() + cam.getNormRightZ() * primaryRay[i][j].getPixelPosX() + cam.getNormUpZ() * primaryRay[i][j].getPixelPosY());
        // update vector normalisation
        primaryRay[i][j].updateNormalisation();

        // while the ray is not intersecting an object and the ray has not marched 100 units
        // create local variable r (the rays step)

        primaryRay[i][j].marchRay(0);
        visibleObjects.clear();
        for (SceneObjects sceneObject1 : sceneObjectsList) {
            if (sceneObject1.objectCulling(primaryRay[i][j])) {
                visibleObjects.add(sceneObject1);
            }
        }
        double distance = 0;
        if (!visibleObjects.isEmpty()) {
            while (distance <= 50 && !primaryRay[i][j].getHit()) {
                // march the ray
                primaryRay[i][j].marchRay(distance);
                // for each object that is hasn't been culled
                for (SceneObjects sceneObject1 : visibleObjects) {
                    // check if the ray intersects the object
                    if (sceneObject1.intersectionCheck(primaryRay[i][j])) {
                        // get the position of the intersection
                        primaryRay[i][j].setHitPoint(primaryRay[i][j].getPosX(), primaryRay[i][j].getPosY(), primaryRay[i][j].getPosZ());
                        // set ray hit to 1
                        primaryRay[i][j].setHit(true);
                        primaryRay[i][j].setHitObject(sceneObject1);
                        // add light amplitude
                        if (sceneObject1.getLuminance() != 0) {
                            primaryRay[i][j].addLightAmplitude(lambertCosineLaw(primaryRay[i][j], sceneObject1) * sceneObject1.getLuminance() * sceneObject1.getReflectivity());
                        }
                    }
                    // hit is already false otherwise
                }
                distance += 0.01;
            }
        }
    }

    public void marchIntersectionLogic(Ray[][] primaryRay, Ray[][] nthRay, List<SceneObjects> sceneObjectsList, int numRays, int numBounces, int lowerx, int upperx, int lowery, int uppery) {
        List<SceneObjects> visibleObjects = new ArrayList<>();
        for (int currentRay = 1; currentRay < numRays; currentRay++) { // sample one ray for each pixel, then move onto the next ray
            for (int j = lowery; j <= uppery; j++) {
                for (int i = lowerx; i <= upperx; i++) {
                    if (primaryRay[i][j].getHit()) {
                        nthRay[i][j] = new Ray(primaryRay[i][j].getHitPointX(), primaryRay[i][j].getHitPointY(), primaryRay[i][j].getHitPointZ());
                        double[][] luminanceArray = new double[numBounces+1][5];
                        // BOUNCES PER RAY
                        // initialize ray starting conditions
                        nthRay[i][j].initializeRay(primaryRay[i][j]);
                        //storeHitData(luminanceArray, nthRay[i][j], -1, nthRay[i][j].getHitObject());
                        for (int currentBounce = 0; currentBounce < numBounces && nthRay[i][j].getHit(); currentBounce++) {
                            if (currentBounce == 0) {
                                // first bounce uses random direction
                                randomDirection(nthRay[i][j], nthRay[i][j].getHitObject());
                            } else {
                                // second uses a reflection vector
                                randomDirection(nthRay[i][j], nthRay[i][j].getHitObject());
                                reflectionBounce(nthRay[i][j], nthRay[i][j].getHitObject());
                            }
                            // add all non culled objects to a list
                            visibleObjects.clear();
                            for (SceneObjects sceneObject1 : sceneObjectsList) {
                                if (sceneObject1.objectCulling(nthRay[i][j])) {
                                    visibleObjects.add(sceneObject1);
                                }
                            }
                            nthRay[i][j].setHit(false);
                            double distance = 0;
                            // march ray and check intersections
                            while (distance <= 25 && !nthRay[i][j].getHit() && !visibleObjects.isEmpty()) { // redundant to check !visibleObjects.isEmpty() every time but the code is cleaner
                                // march the ray
                                nthRay[i][j].marchRay(distance);
                                // CHECK INTERSECTIONS for non-culled objects
                                for (SceneObjects sceneObject1 : visibleObjects) {
                                    if (sceneObject1.intersectionCheck(nthRay[i][j])) {
                                        primaryRay[i][j].addNumHits(); // debug
                                        nthRay[i][j].updateHitProperties(sceneObject1);
                                        // data structure for storing object luminance, dot product and bounce depth, and boolean hit
                                        storeHitData(luminanceArray, nthRay[i][j], currentBounce, sceneObject1);
                                    }
                                }
                                distance += 0.1;
                            }
                        }
                        double brightness = 0;
                        // sum up values of lightness for each bounce into the scene
                        // ((object brightness * lambertCosineLaw) * object reflectivity)
                        if (primaryRay[i][j].getHit()) {
                            for (int index = luminanceArray.length - 1; index >= 0; index--) {
                                if (luminanceArray[index][3] == 1) {
                                    brightness = ((luminanceArray[index][0] + brightness) * luminanceArray[index][1]) * luminanceArray[index][4];
                                }
                            }
                        }
                        primaryRay[i][j].addLightAmplitude(brightness / numRays);
                    }
                }
            }
        }
    }

    public synchronized void storeHitData(double[][] luminanceArray, Ray nthRay, int currentBounce, SceneObjects sceneObject){
        int pos = currentBounce + 1;
        luminanceArray[pos][0] = sceneObject.getLuminance(); // object luminance
        luminanceArray[pos][1] = lambertCosineLaw(nthRay, sceneObject); // dot product
        luminanceArray[pos][2] = currentBounce + 1; // which bounce
        luminanceArray[pos][3] = 1; // boolean hit
        luminanceArray[pos][4] = sceneObject.getReflectivity(); // reflectivity

        if (currentBounce == -1){
            luminanceArray[pos][1] = 1;
        }
    }

    public void randomDirection(Ray nthRay, SceneObjects sceneObject) {
        double dotproduct = -1;
        Random random = new Random();

        nthRay.marchRay(0);
        sceneObject.calculateNormal(nthRay);

        while (dotproduct <= 0){
            // Generate a random direction uniformly on a sphere
            double theta = Math.acos(2 * random.nextDouble() - 1); // polar angle
            double phi = 2 * Math.PI * random.nextDouble(); // azimuthal angle

            nthRay.setDirX(Math.sin(theta) * Math.cos(phi));
            nthRay.setDirY(Math.sin(theta) * Math.sin(phi));
            nthRay.setDirZ(Math.cos(theta));

            // Normalize the random direction
            nthRay.updateNormalisation();

            // Calculate the dot product
            dotproduct = sceneObject.getNormalX() * nthRay.getDirX() + sceneObject.getNormalY() * nthRay.getDirY() + sceneObject.getNormalZ() * nthRay.getDirZ();
        }
        nthRay.updateOrigin(0.15); // march the ray a tiny amount to move it off the sphere
    }

    // R = I - 2 * (I dot N) * N
    public void reflectionBounce(Ray nthRay, SceneObjects sceneObject) {
        double dotproduct = sceneObject.getNormalX() * nthRay.getDirX() + sceneObject.getNormalY() * nthRay.getDirY() + sceneObject.getNormalZ() * nthRay.getDirZ();
        sceneObject.calculateNormal(nthRay);
        double reflectionX = nthRay.getDirX() - 2 * (dotproduct) * sceneObject.getNormalX();
        double reflectionY = nthRay.getDirY() - 2 * (dotproduct) * sceneObject.getNormalY();
        double reflectionZ = nthRay.getDirZ() - 2 * (dotproduct) * sceneObject.getNormalZ();

        nthRay.setDirection(reflectionX, reflectionY, reflectionZ);
        nthRay.updateNormalisation();
        nthRay.updateOrigin(0.15); // march the ray a tiny amount to move it off the sphere
    }

    public double lambertCosineLaw(Ray currentRay, SceneObjects sceneObject) {
        sceneObject.calculateNormal(currentRay);
        currentRay.updateNormalisation();

        // dot product of sphere normal and ray direction
        double costheta = Math.abs(sceneObject.getNormalX() * currentRay.getDirX() + sceneObject.getNormalY() * currentRay.getDirY() + sceneObject.getNormalZ() * currentRay.getDirZ());
        return costheta;
    }

    public void debugDrawScreenBrightness(Camera cam, Ray[][] primaryRay) {
        DecimalFormat df = new DecimalFormat("#.00");
        // iterate through each rays hit value and print the output
        for (int i = 0; i < cam.getResX(); i++) {
            System.out.print("------");
        }
        System.out.println(" ");
        for (int j = 0; j < cam.getResY(); j++) {
            System.out.print("|");
            for (int i = 0; i < cam.getResX(); i++) {
                if (primaryRay[i][j].getHit()) {
                    if (primaryRay[i][j].getLightAmplitude() >= 10) {
                        System.out.print(df.format(primaryRay[i][j].getLightAmplitude()) + "|");
                    } else if (primaryRay[i][j].getLightAmplitude() >= 1.0 && primaryRay[i][j].getLightAmplitude() < 10) {
                        System.out.print("0" + df.format(primaryRay[i][j].getLightAmplitude()) + "|");
                    } else if (primaryRay[i][j].getLightAmplitude() < 1) {
                        System.out.print("00" + df.format(primaryRay[i][j].getLightAmplitude()) + "|");
                    }
                } else {
                    System.out.print("00.00|");
                }
            }
            System.out.println(" ");
        }
        for (int i = 0; i < cam.getResX(); i++) {
            System.out.print("------");
        }
    }

    public void debugDrawScreenNumHits(Camera cam, Ray[][] primaryRay) {
        DecimalFormat df = new DecimalFormat("#.00");
        // iterate through each rays hit value and print the output
        for (int i = 0; i < cam.getResX(); i++) {
            System.out.print("------");
        }
        System.out.println(" ");
        for (int j = 0; j < cam.getResY(); j++) {
            System.out.print("|");
            for (int i = 0; i < cam.getResX(); i++) {
                if (primaryRay[i][j].getHit()) {
                    if (primaryRay[i][j].getNumHits() >= 10) {
                        System.out.print(df.format(primaryRay[i][j].getNumHits()) + "|");
                    } else if (primaryRay[i][j].getNumHits() >= 1.0 && primaryRay[i][j].getNumHits() < 10) {
                        System.out.print("0" + df.format(primaryRay[i][j].getNumHits()) + "|");
                    } else if (primaryRay[i][j].getNumHits() < 1) {
                        System.out.print("00" + df.format(primaryRay[i][j].getNumHits()) + "|");
                    }
                } else {
                    System.out.print("00.00|");
                }
            }
            System.out.println(" ");
        }
        for (int i = 0; i < cam.getResX(); i++) {
            System.out.print("------");
        }
    }
}
