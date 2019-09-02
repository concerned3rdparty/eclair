/*
 * Copyright 2019 ACINQ SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.acinq.eclair.cli.general;

import fr.acinq.eclair.cli.utils.BaseSubCommand;
import okhttp3.ResponseBody;
import picocli.CommandLine;

import static fr.acinq.eclair.cli.utils.Utils.print;

@CommandLine.Command(name = "channelstats", description = "Aggregate statistics about the routing activity of each channels. Amounts are in satoshis.", sortOptions = false)
public class ChannelsStats extends BaseSubCommand {

  @Override
  public Integer call() throws Exception {
    final ResponseBody body = http("channelstats");
    print(body.string());
    return 0;
  }
}