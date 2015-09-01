package sybrix.easygsp2;



public class RequestThreadLocal {

        private static final ThreadLocal<RequestResponseObject> _id = new ThreadLocal<RequestResponseObject>(){

                protected RequestResponseObject initialValue() {
                        return null;
                }


        };

        public static RequestResponseObject get(){
                return _id.get();
        }

        protected static void set(RequestResponseObject id){
                _id.set(id);
        }

        public static void remove() {
                try {
                        _id.remove();
                }catch (Throwable e){
                        //do nothing
                }
        }
}
