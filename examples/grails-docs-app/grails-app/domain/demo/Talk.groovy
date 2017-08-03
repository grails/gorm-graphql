// tag::wholeFile[]
package demo

class Talk {

    String title
    int duration

    static belongsTo = [speaker: Speaker]
}
// end::wholeFile[]
