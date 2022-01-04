package io.github.ushiosan23.resgen.generators;

import java.io.IOException;

public interface IGenerator {

    /**
     * Create files if it´s necessary
     *
     * @throws IOException Error to create files
     */
    void createIfIsNeed() throws IOException;

    /**
     * Generate files and write all data
     *
     * @throws IOException Error to generate it
     */
    void generate() throws IOException;

}
