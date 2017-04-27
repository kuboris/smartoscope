package cz.binarytrio.molescope.application;

/**
 * Created by nicko on 4/20/17
 */

public class MoleApp {

    public static final String STORAGE_CONNECTION_STRING = "FileEndpoint=https://triodisks597.file.core.windows.net;SharedAccessSignature=sv=2016-05-31&ss=f&srt=sco&sp=r&se=2017-07-20T07:49:14Z&st=2017-04-19T23:49:14Z&spr=https&sig=0S3RaPQq0s716TjlyAUE4RaSaXHpYO5zTXkTjCJjQkQ%3D";
    public static final String SHARE_NAME = "trio-apk-storage";
    public static final String MODEL_NAME = "tensorflow_inception_graph.pb";
    public static final String VERSIONFILE_EXTENSION = ".ver";

    public static final int MODEL_INPUT_SIZE = 299;
    public static final int MODEL_IMAGE_MEAN = 128;
    public static final float MODEL_IMAGE_STD = 128;
    public static final String MODEL_INPUT_NAME = "Mul";
    public static final String MODEL_OUTPUT_NAME = "final_result";
}
