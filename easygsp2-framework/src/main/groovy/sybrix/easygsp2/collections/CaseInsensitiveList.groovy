package sybrix.easygsp2.collections

class CaseInsensitiveList extends ArrayList<String>{
        @Override
        boolean add(String s) {
                return super.add(s.toUpperCase())
        }

        @Override
        void add(int index, String element) {
                super.add(index, element.toUpperCase())
        }

        @Override
        boolean contains(Object o) {
                return super.contains(((String)o).toUpperCase())
        }

        @Override
        String set(int index, String element) {
                return super.set(index, element.toUpperCase())
        }

        @Override
        boolean remove(Object o) {
                return super.remove(((String)o).toUpperCase())
        }

        @Override
        int lastIndexOf(Object o) {
                return super.lastIndexOf(((String)o).toUpperCase())
        }

}
