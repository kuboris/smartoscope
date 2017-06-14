package cz.binarytrio.molescope.application;

/**
 * Created by nicko on 4/20/17
 */

public class MoleApp {
    public static final String STORAGE_CONNECTION_STRING = "FileEndpoint=https://moledisk.file.core.windows.net;SharedAccessSignature=sv=2016-05-31&ss=f&srt=sco&sp=r&se=2018-06-14T23:59:05Z&st=2016-06-14T15:59:05Z&spr=https&sig=bn0NbZjn4cbcN3AaRRHNfvitBdugwjg9w35%2BaQZAwh0%3D";
    public static final String SHARE_NAME = "model";
    public static final String MODEL_NAME = "tensorflow_inception_graph.pb";
    public static final String VERSIONFILE_EXTENSION = ".ver";
    public static final String TMPFILE_EXTENSION = ".tmp";

    public static final int MODEL_INPUT_SIZE = 299;
    public static final int MODEL_IMAGE_MEAN = 128;
    public static final float MODEL_IMAGE_STD = 128;
    public static final String MODEL_INPUT_NAME = "Mul";
    public static final String MODEL_OUTPUT_NAME = "final_result";
}
