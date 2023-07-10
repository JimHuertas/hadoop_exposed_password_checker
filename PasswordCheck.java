import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordCheck {

    public static class PasswordMapper extends Mapper<Object, Text, Text, IntWritable> {

        private static final IntWritable one = new IntWritable(1);
        private Text password = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // Obtener la contrasena de entrada de los argumentos de linea de comandos
            String inputPassword = context.getConfiguration().get("password");

            // Realizar el hash SHA-1 de la contrasena de entrada
            String sha1Hash = generateSHA1(inputPassword);

            // Dividir la linea del archivo en sha1Hash y coincidencia_de_veces_filtrada
            String[] parts = value.toString().split(":");

            // Verificar si la contrasena coincide con el hash SHA-1 en el archivo de entrada
            if (parts[0].equals(sha1Hash)) {
                String respuesta = "De un total de: Gb\nSe Encontro la contrasenia vulnerada\nOriginal Password: " + inputPassword + "\nSha1:" + parts[0]+ "\nNro de veces usada antes: " + parts[1];
                password.set(respuesta);
                // password.set(parts[0]);
                context.write(password, one);
            }// } else{
            //     String respuesta = "De un total de: Gb\n No se Encontraron coincidencias";
            //     password.set(respuesta);
            //     context.write(password, one);
            // }
        }

        public String generateSHA1(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] inputData = input.getBytes(StandardCharsets.UTF_8);
                byte[] sha1Hash = md.digest(inputData);

                StringBuilder sb = new StringBuilder();
                for (byte b : sha1Hash) {
                    sb.append(String.format("%02X", b));  // Usar "%02X" para formato en mayscula
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class PasswordReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int totalCount = 0;
            for (IntWritable value : values) {
                totalCount += value.get();
            }
            result.set(totalCount);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: PasswordCheck <input-file> <output-directory> <password>");
            System.exit(1);
        }

        Configuration conf = new Configuration();
        conf.set("password", args[2]);

        Job job = Job.getInstance(conf, "Password Verifier");

        job.setJarByClass(PasswordCheck.class);
        job.setMapperClass(PasswordMapper.class);
        job.setCombinerClass(PasswordReducer.class);
        job.setReducerClass(PasswordReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
