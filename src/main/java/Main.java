import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.util.Bytes;

import javax.management.Descriptor;
import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args){
        Configuration config= HBaseConfiguration.create();

        try {
            HBaseAdmin admin = new HBaseAdmin(config);
            TableName tableName=TableName.valueOf("test");
            HTableDescriptor htd = new HTableDescriptor(tableName);
            HColumnDescriptor hcd = new HColumnDescriptor("data");
            htd.addFamily(hcd);
            admin.createTable(htd);
            HTableDescriptor[] tables =admin.listTables();
            if(tables.length != 1 && Bytes.equals(tableName.getName(),tables[0].getTableName().getName())){
                throw new IOException("Failed creation of table");
            }
            HTable table = new HTable(config, tableName);
            try {
                for (int i = 1; i <= 3; i++) {
                    byte[] row = Bytes.toBytes("row" + 1);
                    Put put = new Put(row);
                    byte[] columnFamily = Bytes.toBytes("data");
                    byte[] qualifier = Bytes.toBytes(String.valueOf(i));
                    byte[] value = Bytes.toBytes("value" + i);
                    put.add(columnFamily, qualifier, value);
                    table.put(put);
                }
                Get get = new Get(Bytes.toBytes("row1"));
                Result result = table.get(get);
                System.out.println("Get: " + result);
                Scan scan = new Scan();
                ResultScanner scanner = table.getScanner(scan);

                try {
                    for (Result scannerResult : scanner) {
                        System.out.println("Scan: " + scannerResult);
                    }

                } finally {
                    scanner.close();
                }
                admin.disableTable(tableName);
                admin.deleteTable(tableName);


            }finally{
                table.close();
            }
            admin.close();
        }
        catch(IOException ex){
            System.out.println("darn");
        }

    }
}
