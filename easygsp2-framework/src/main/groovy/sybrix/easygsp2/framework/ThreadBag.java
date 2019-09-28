package sybrix.easygsp2.framework;


public class ThreadBag {

        private static final ThreadLocal<ThreadVariables> _id = new ThreadLocal<ThreadVariables>() {

                protected ThreadVariables initialValue() {
                        return null;
                }
        };

        public static ThreadVariables get() {
                return _id.get();
        }

        public static void set(ThreadVariables id) {
                _id.set(id);
        }

        public static void remove() {
                try {
                        if (_id.get().getSql() != null) {
                                _id.get().getSql().close();
                        }
                } catch (Throwable e) {
                        //do nothing
                }

                try {
                        _id.set(null);
                        _id.remove();
                } catch (Throwable e) {
                        //do nothing
                }
        }
}
