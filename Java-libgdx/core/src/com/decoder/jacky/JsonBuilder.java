package com.decoder.jacky;

public class JsonBuilder {
    private StringBuilder builder = new StringBuilder();

    private boolean isInsert=false;

    public Dict setBasicTypeAsDict(){
        return new Dict(0);
    }
    public List setBasicTypeAsList(){
        return new List(0);
    }
    public void insert(Dict dict){builder.insert(dict.index,dict.exitDict());}
    public void insert(List list){builder.insert(list.index,list.exitList());}

    @Override
    public String toString() {
        return builder.toString();
    }

    public static class Dict {
        private StringBuilder builder = new StringBuilder("{");
        int index;

        Dict(int index){
            this.index=index;
        }

        public void insert(Dict dict){builder.insert(dict.index,dict.exitDict()).append(",");}
        public void insert(List list){builder.insert(list.index,list.exitList()).append(",");}

        public void addKeyValue(String key, String value) {
            if (value==null||value.equals("null"))
                builder.append("\"").append(key).append("\":").append("null").append(",");
            else
                builder.append("\"").append(key).append("\":").append("\"").append(value).append("\",");
        }

        public void addKeyValue(String key, float value) {
            builder.append("\"").append(key).append("\":").append(value).append(",");
        }

        public void addKeyValue(String key, int value) {
            builder.append("\"").append(key).append("\":").append(value).append(",");
        }

        public void addKeyValue(String key, boolean value) {
            builder.append("\"").append(key).append("\":").append(value).append(",");
        }

        public Dict addKeyDict(String key) {
            builder.append("\"").append(key).append("\":");
            return new Dict(builder.length());
        }
        public List addKeyList(String key) {
            builder.append("\"").append(key).append("\":");

            return new List(builder.length());
        }

        String exitDict() {
            int index = builder.lastIndexOf(",");
            if (index != -1)
                builder.deleteCharAt(index);
            builder.append("}");

            return builder.toString();

        }
    }

    public static class List {
        private StringBuilder builder = new StringBuilder("[");
        int index;

        List(int index) {
            this.index = index;
        }

        public void insert(Dict dict) {
            builder.insert(dict.index, dict.exitDict()).append(",");
        }

        public void insert(List list) {
            builder.insert(list.index, list.exitList()).append(",");
        }

        public void addValue(String value) {
            builder.append("\"").append(value).append("\",");
        }

        public void addValue(float value) {
            builder.append(value).append(",");

        }

        public void addValue(boolean value) {
            builder.append(value).append(",");
        }

        public void addValue(float[] value){

        }
        public void addValue(int[] value){

        }

        public Dict addDict() {
            return new Dict(builder.length());
        }

        public List addList() {
            return new List(builder.length());
        }

        String exitList() {
            int index = builder.lastIndexOf(",");
            if (index != -1)
                builder.deleteCharAt(index);

            builder.append("]");
            return builder.toString();
        }

    }

    public static void main(String[] args) {
        JsonBuilder val = new JsonBuilder();

        Dict basic=val.setBasicTypeAsDict();

        basic.addKeyValue("233,23","2222");
        basic.addKeyValue("2222",1234f);

        Dict value=basic.addKeyDict("233");

        value.addKeyValue("333",2222);

        basic.insert(value);

        val.insert(basic);

        System.out.println(val.toString());


    }
}
